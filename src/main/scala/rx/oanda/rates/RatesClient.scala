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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import cats.data.Xor
import de.knutwalker.akka.stream.support.CirceStreamSupport
import io.circe.Decoder
import rx.oanda.OandaEnvironment.{ConnectionPool, Auth}
import rx.oanda.errors.OandaError._
import rx.oanda.rates.candles.{CandleTypes, CandleType, CandleGranularity, CandleGranularities}
import CandleGranularities.S5
import rx.oanda.rates.RatesClient._
import rx.oanda.utils.Heartbeat
import rx.oanda.{StreamingConnection, ApiConnection, OandaEnvironment}

import scala.util._

class RatesClient[A <: Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool[A])
  extends ApiConnection with StreamingConnection {

  private[oanda] val streamingConnection = env.streamFlow[Long]
  private[oanda] val apiConnection = env.apiFlow[Long]

  private[oanda] def pricesStreamRequest(accountId: Long, instruments: Seq[String], sessionId: String = "undefined"): HttpRequest = {
    val rawQuery = Seq(accountIdQuery(accountId), instrumentsQuery(instruments), s"sessionId=$sessionId").filter(_.nonEmpty).mkString("&")
    HttpRequest(GET, Uri(s"/v1/prices").withRawQueryString(rawQuery), headers = env.headers)
  }

  private[oanda] def pricesReq(instruments: Seq[String]): HttpRequest = {
    HttpRequest(GET, Uri(s"/v1/prices").withRawQueryString(instrumentsQuery(instruments)), headers = env.headers)
  }

  private[oanda] def pricesSinceReq(instruments: Seq[String], since: Long): HttpRequest = {
    val rawQuery = Seq(instrumentsQuery(instruments), s"since=$since").filter(_.nonEmpty).mkString("&")
    HttpRequest(GET, Uri(s"/v1/prices").withRawQueryString(rawQuery), headers = env.headers)
  }

  private[oanda] def instrumentsRequest(accountId: Long, instruments: Seq[String] = Nil): HttpRequest = {
    val rawQuery = Seq(accountIdQuery(accountId), instrumentsQuery(instruments), fieldsQuery).filter(_.nonEmpty).mkString("&")
    HttpRequest(GET, Uri(s"/v1/instruments").withRawQueryString(rawQuery), headers = env.headers)
  }

  def livePricesStream(accountID: Long, instruments: Seq[String], sessionId: String = "undefined"): Source[Xor[Price, Heartbeat], NotUsed] =
    startStreaming[Price](pricesStreamRequest(accountID, instruments), "tick").log("price")

  def latestPrices(instruments: Seq[String]): Source[Price, NotUsed] =
    makeRequest[Vector[Price]](pricesReq(instruments)).log("prices").mapConcat(identity).log("price")

  def historicalPrices(instruments: Seq[String], after: Long): Source[Price, NotUsed] =
    makeRequest[Vector[Price]](pricesSinceReq(instruments, after)).log("prices-since").mapConcat(identity).log("price")

  /**
    * Get a list of tradeable instruments (currency pairs, CFDs, and commodities) that are available for trading with the account specified.
    *
    * @param accountId   The account id to fetch the list of tradeable instruments for.
    * @param instruments A list of instruments that are to be returned. If the instruments list is empty, all instruments will be returned.
    * @return A Source to retrieve infos about all or the specified instruments.
    */
  def instruments(accountId: Long, instruments: Seq[String] = Nil): Source[Instrument, NotUsed] =
    makeRequest[Vector[Instrument]](instrumentsRequest(accountId, instruments)).log("instruments").mapConcat(identity).log("instrument")

  def latestCandles[R](instrument: String, count: Int = 500, granularity: CandleGranularity = S5, candleType: CandleTypes.Aux[R] = CandleTypes.BidAsk): Source[R, NotUsed] = {
    val req = HttpRequest(GET, Uri("/v1/candles").withQuery(Query(Map("instrument" → instrument, "candleFormat" → candleType.uriParam, "granularity" → granularity.toString, "count" → count.toString))), headers = env.headers)
    makeRequest[Vector[candleType.R]](req)(candleType.decoder).log("instrument-history-count").mapConcat(identity).log("candle")
  }

  def historicalCandles[R](instrument: String, startTime: Long, endTime: Long, granularity: CandleGranularity = S5, candleType: CandleTypes.Aux[R] = CandleTypes.BidAsk, includeFirst: Boolean = true): Source[R, NotUsed] = {
    val req = HttpRequest(GET, Uri("/v1/candles").withQuery(Query(Map("instrument" → instrument, "candleFormat" → candleType.uriParam, "granularity" → granularity.toString, "start" → startTime.toString, "end" → endTime.toString, "includeFirst" → includeFirst.toString))), headers = env.headers)
    makeRequest[Vector[candleType.R]](req)(candleType.decoder).log("instrument-history-date").mapConcat(identity).log("candle")
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
