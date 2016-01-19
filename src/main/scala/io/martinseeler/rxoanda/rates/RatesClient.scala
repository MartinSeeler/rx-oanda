/*
 * Copyright 2015 – 2016 Martin Seeler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.martinseeler.rxoanda.rates

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.io.Framing
import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.data.Xor
import cats.data.Xor.{Left, Right}
import io.circe.Decoder
import io.circe.parse._
import io.martinseeler.rxoanda.errors.OandaError.OandaErrorEntityConversion
import io.martinseeler.rxoanda.{Heartbeat, OandaEnvironment}

import scala.util.{Failure, Success}

object RatesClient {

  def rates(accountID: String, instruments: Seq[String])(implicit env: OandaEnvironment, mat: ActorMaterializer): Source[Xor[Heartbeat, OandaTick], Unit] = {
    val params = Map("accountId" → accountID, "instruments" → instruments.mkString(","))
    val req = HttpRequest(GET, Uri(s"/v1/prices").withQuery(Query(params)), headers = env.headers)
    Source.single(req → 42L).log("request")
      .via(env.streamConnection).log("response")
      .flatMapConcat {
        case (Success(HttpResponse(StatusCodes.OK, header, entity, _)), _) ⇒
          entity.dataBytes.log("data-bytes")
            .via(Framing.delimiter(ByteString("\n"), 1024)).log("frame-delimiter")
            .map(_.utf8String).log("utf8")
            .map(json ⇒ decode(json)(Decoder.decodeXor[Heartbeat, OandaTick]("heartbeat", "tick"))).log("decode-xor")
            .flatMapConcat {
              case Left(e) ⇒ Source.failed(e)
              case Right(x) ⇒ Source.single(x)
            }
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.failed(new Exception("Unknown state in rates"))
      }
  }

}
