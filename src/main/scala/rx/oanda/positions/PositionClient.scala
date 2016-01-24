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

package rx.oanda.positions

import akka.actor.ActorSystem
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.ETag
import akka.http.scaladsl.model.{StatusCodes, HttpResponse, Uri, HttpRequest}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import de.knutwalker.akka.stream.support.CirceStreamSupport
import io.circe.Decoder
import rx.oanda.errors.OandaError._
import rx.oanda.OandaEnvironment
import rx.oanda.OandaEnvironment._

import scala.util.{Failure, Success}

class PositionClient[A <: Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ApiFlow[A]) {

  private[this] val apiConnections = env.apiFlow[Long]

  private def makeRequest[R](req: HttpRequest)(implicit ev: Decoder[R]): Source[R, Unit] =
    Source.single(req → 42L).log("request")
      .via(apiConnections).log("response")
      .flatMapConcat {
        case (Success(HttpResponse(StatusCodes.OK, header, entity, _)), _) ⇒
          entity.dataBytes
            .via(Gzip.decoderFlow)
            .via(CirceStreamSupport.decode[R]).log("decode")
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.empty
      }

  def positions(accountId: Long): Source[Position, Unit] = {
    val req = HttpRequest(GET, Uri(s"/v1/accounts/$accountId/positions"), headers = env.headers)
    makeRequest[Vector[Position]](req).mapConcat(identity)
  }

  def position(accountId: Long, instrument: String): Source[Position, Unit] = {
    val req = HttpRequest(GET, Uri(s"/v1/accounts/$accountId/positions/$instrument"), headers = env.headers)
    makeRequest[Position](req)
  }

  def closePosition(accountId: Long, instrument: String): Source[PositionCloseEvent, Unit] = {
    val req = HttpRequest(DELETE, Uri(s"/v1/accounts/$accountId/positions/$instrument"), headers = env.headers)
    makeRequest[PositionCloseEvent](req)
  }

}
