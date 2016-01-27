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
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.io.Framing
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import cats.data.Xor
import de.knutwalker.akka.stream.support.CirceStreamSupport
import io.circe.Decoder
import rx.oanda.OandaEnvironment.{ConnectionPool, Auth}
import rx.oanda.errors.OandaError._
import rx.oanda.rates.RatesClient._
import rx.oanda.utils.Heartbeat
import rx.oanda.{ApiConnection, OandaEnvironment}

import scala.util._

class RatesClient[A <: Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool[A])
  extends ApiConnection {

  private[oanda] def streamConnections: Flow[(HttpRequest, Long), (Try[HttpResponse], Long), HostConnectionPool] = env.connectionFlow[Long](env.streamEndpoint)

  def rates(accountID: Long, instruments: Seq[String]): Source[Xor[Heartbeat, OandaTick], Unit] = {
    val params = Map("accountId" → accountID.toString, "instruments" → instruments.mkString(","))
    val req = HttpRequest(GET, Uri(s"/v1/prices").withQuery(Query(params)), headers = env.headers)
    Source.single(req → 42L).log("request")
      .via(streamConnections).log("response")
      .flatMapConcat {
        case (Success(HttpResponse(StatusCodes.OK, header, entity, _)), _) ⇒
          entity.dataBytes.log("chunks", _.utf8String)
            .via(Framing.delimiter(ByteString("\n"), 1024, allowTruncation = true)).log("bytes", _.utf8String)
            .via(CirceStreamSupport.decode(Decoder.decodeXor[Heartbeat, OandaTick]("heartbeat", "tick"))).log("decode")
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.failed(new Exception("Unknown state in rates"))
      }
  }

  private[oanda] val apiConnections = env.connectionFlow[Long](env.apiEndpoint)

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
