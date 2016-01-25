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

package rx.oanda.rates

import akka.actor.ActorSystem
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import de.knutwalker.akka.stream.support.CirceStreamSupport
import io.circe.Decoder
import rx.oanda.OandaEnvironment
import rx.oanda.OandaEnvironment.{ApiFlow, Auth}
import rx.oanda.errors.OandaError.OandaErrorEntityConversion
import rx.oanda.rates.RatesClient._

import scala.util.{Failure, Success}

class RatesClient[A <: Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ApiFlow[A]) {

  /*def rates(accountID: String, instruments: Seq[String])(implicit env: OandaEnvironment, mat: ActorMaterializer): Source[Xor[Heartbeat, OandaTick], Unit] = {
    val params = Map("accountId" → accountID, "instruments" → instruments.mkString(","))
    val req = HttpRequest(GET, Uri(s"/v1/prices").withQuery(Query(params)), headers = env.headers)
    Source.single(req → 42L).log("request")
      .via(env.streamConnection).log("response")
      .flatMapConcat {
        case (Success(HttpResponse(StatusCodes.OK, header, entity, _)), _) ⇒
          entity.dataBytes.log("data-bytes")
            .via(Framing.delimiter(ByteString("\n"), 1024)).log("frame-delimiter")
            .via(CirceStreamSupport.decode(Decoder.decodeXor[Heartbeat, OandaTick]("heartbeat", "tick"))).log("decode-xor")
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.failed(new Exception("Unknown state in rates"))
      }
  }*/

  private[this] val apiConnections = env.apiFlow[Long]

  private def makeRequest[R](req: HttpRequest)(implicit ev: Decoder[R]): Source[R, Unit] =
    Source.single(req → 42L).log("request")
      .via(apiConnections).log("response")
      .flatMapConcat {
        case (Success(HttpResponse(StatusCodes.OK, header, entity, _)), _) ⇒
          entity.dataBytes
            .via(Gzip.decoderFlow).log("bytes")
            .via(CirceStreamSupport.decode[R]).log("decode")
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.empty
      }

  def prices(instruments: Seq[String]): Source[OandaTick, Unit] = {
    val req = HttpRequest(GET, Uri(s"/v1/prices").withRawQueryString(instrumentsQuery(instruments)), headers = env.headers)
    makeRequest[Vector[OandaTick]](req).mapConcat(identity)
  }

  def instruments(accountId: Long, instruments: Seq[String] = Nil): Source[Instrument, Unit] = {
    val rawQuery = Seq(accountIdQuery(accountId), fieldsQuery, instrumentsQuery(instruments)).filter(_.nonEmpty).mkString("&")
    val req = HttpRequest(GET, Uri(s"/v1/instruments").withRawQueryString(rawQuery), headers = env.headers)
    makeRequest[Vector[Instrument]](req).mapConcat(identity)
  }

}

object RatesClient {

  private val instrumentFields = Seq(
    "displayName",
    "halted",
    "interestRate",
    "marginRate",
    "maxTradeUnits",
    "maxTrailingStop",
    "minTrailingStop",
    "pip",
    "precision"
  )

  private val fieldsQuery = s"fields=${instrumentFields.mkString("%2C")}"

  private def instrumentsQuery(instruments: Seq[String]): String =
    if (instruments.isEmpty) "" else s"instruments=${instruments.mkString("%2C")}"

  private def accountIdQuery(accountId: Long): String =
    s"accountId=$accountId"

}
