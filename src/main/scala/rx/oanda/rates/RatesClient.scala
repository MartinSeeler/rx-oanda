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
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import cats.data.Xor
import rx.oanda.OandaEnvironment.{Auth, ConnectionPool}
import rx.oanda.rates.RatesClient._
import rx.oanda.rates.candles.CandleGranularities.S5
import rx.oanda.rates.candles.{CandleGranularity, CandleTypes}
import rx.oanda.utils.Heartbeat
import rx.oanda.utils.QueryHelper._
import rx.oanda.{ApiConnection, OandaEnvironment, StreamingConnection}

class RatesClient[A <: Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool[A])
  extends ApiConnection with StreamingConnection {

  private[oanda] val streamingConnection = env.streamFlow[Long]
  private[oanda] val apiConnection = env.apiFlow[Long]

  private[oanda] def pricesStreamRequest(accountId: Long, instruments: Seq[String], sessionId: String = "undefined"): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/prices").withRawQueryString(rawQueryStringOf(accountIdParam(accountId) :: optionalInstrumentsParam(instruments) :: optionalSessionIdParam(Some(sessionId)) :: Nil)), headers = env.headers)

  private[oanda] def pricesReq(instruments: Seq[String], since: Option[Long]): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/prices").withRawQueryString(rawQueryStringOf(optionalInstrumentsParam(instruments) :: optionalSinceParam(since) :: Nil)), headers = env.headers)

  private[oanda] def instrumentsRequest(accountId: Long, instruments: Seq[String] = Nil): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/instruments").withRawQueryString(rawQueryStringOf(accountIdParam(accountId) :: optionalInstrumentsParam(instruments) :: fieldsQuery :: Nil)), headers = env.headers)

  def livePrices(accountID: Long, instruments: Seq[String], sessionId: String = "undefined"): Source[Xor[Price, Heartbeat], NotUsed] =
    startStreaming[Price](pricesStreamRequest(accountID, instruments), "tick").log("price")

  def prices(instruments: Seq[String]): Source[Price, NotUsed] =
    makeRequest[Vector[Price]](pricesReq(instruments, None)).log("prices").mapConcat(identity).log("price")

  def pricesSince(instruments: Seq[String], since: Long): Source[Price, NotUsed] =
    makeRequest[Vector[Price]](pricesReq(instruments, Some(since))).log("prices").mapConcat(identity).log("price")

  /**
    * Get a list of tradeable instruments (currency pairs, CFDs, and commodities) that are available for trading with the account specified.
    *
    * @param accountId   The account id to fetch the list of tradeable instruments for.
    * @param instruments A list of instruments that are to be returned. If the instruments list is empty, all instruments will be returned.
    * @return A Source to retrieve infos about all or the specified instruments.
    */
  def instruments(accountId: Long, instruments: Seq[String] = Nil): Source[Instrument, NotUsed] =
    makeRequest[Vector[Instrument]](instrumentsRequest(accountId, instruments)).log("instruments").mapConcat(identity).log("instrument")

  def candles[R](instrument: String, count: Int = 500, granularity: CandleGranularity = S5, candleType: CandleTypes.Aux[R] = CandleTypes.BidAsk): Source[R, NotUsed] = {
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

}
