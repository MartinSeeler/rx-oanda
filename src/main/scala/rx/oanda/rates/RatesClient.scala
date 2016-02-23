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
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import cats.data.Xor
import rx.oanda.OandaEnvironment.{Auth, ConnectionPool}
import rx.oanda.rates.RatesClientRequests._
import rx.oanda.rates.candles.CandleGranularities.S5
import rx.oanda.rates.candles.{CandleGranularity, CandleTypes}
import rx.oanda.utils.Heartbeat
import rx.oanda.{ApiConnection, OandaEnvironment, StreamingConnection}

class RatesClient[A <: Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool[A])
  extends ApiConnection with StreamingConnection {

  private[oanda] val streamingConnection = env.streamFlow[Long]
  private[oanda] val apiConnection = env.apiFlow[Long]

  /**
    * Get all tradeable instruments (currency pairs, CFDs, and commodities) that are available for trading with the account specified.
    *
    * @param accountId The account id to fetch the list of tradeable instruments for.
    * @return A source which emits all available `Instrument`s.
    */
  def allInstruments(accountId: Long): Source[Instrument, NotUsed] =
    makeRequest[Vector[Instrument]](instrumentsRequest(accountId, Nil).withHeaders(env.headers))
      .log("instruments").mapConcat(identity).log("instrument")

  /**
    * Get a list of tradeable instruments (currency pairs, CFDs, and commodities) that are available for trading with the account specified.
    *
    * @param accountId   The account id to fetch the list of tradeable instruments for.
    * @param instruments A list of instruments that are to be returned.
    *                    If the list is empty, all instruments will be returned.
    * @return A source which emits the requested `Instrument`s.
    */
  def instruments(accountId: Long, instruments: Seq[String]): Source[Instrument, NotUsed] =
    makeRequest[Vector[Instrument]](instrumentsRequest(accountId, instruments).withHeaders(env.headers))
      .log("instruments").mapConcat(identity).log("instrument")

  /**
    * Get a specific instruments (currency pairs, CFDs, and commodities) that is available for trading with the account specified.
    *
    * @param accountId  The account id to fetch the tradeable instrument for.
    * @param instrument Name of the instrument to retrieve history for.
    *                   The value should be one of the available instrument codes from `instruments`.
    * @return A source which emits the single `Instrument`.
    */
  def instrument(accountId: Long, instrument: String): Source[Instrument, NotUsed] =
    makeRequest[Vector[Instrument]](instrumentsRequest(accountId, instrument :: Nil).withHeaders(env.headers))
      .log("instruments").mapConcat(identity).log("instrument")

  /**
    * Fetch prices for specified instruments that are available on the OANDA platform.
    *
    * @param instruments A list of instruments to fetch prices for.
    *                    Values should be one of the available instrument from `instruments`.
    * @return A source with prices for each instrument requested.
    */
  def prices(instruments: Seq[String]): Source[Price, NotUsed] =
    makeRequest[Vector[Price]](pricesRequest(instruments, None).withHeaders(env.headers))
      .log("prices").mapConcat(identity).log("price")

  /**
    * Fetch the price for a specified instrument that are available on the OANDA platform.
    *
    * @param instrument Name of the instrument to retrieve history for.
    *                   The value should be one of the available instrument codes from `instruments`.
    * @return A source with the latest price for the instrument requested.
    */
  def price(instrument: String): Source[Price, NotUsed] =
    makeRequest[Vector[Price]](pricesRequest(instrument :: Nil, None).withHeaders(env.headers))
      .log("prices").mapConcat(identity).log("price")

  /**
    * Fetch prices for specified instruments that are available on the OANDA platform.
    *
    * @param instruments A list of instruments to fetch prices for.
    *                    Values should be one of the available instrument from `instruments`.
    * @param since       Only prices that occurred after the specified timestamp are returned.
    *                    The value specified must be in a valid datetime format.
    * @return A source with prices for each instrument requested.
    */
  def pricesSince(instruments: Seq[String], since: Long): Source[Price, NotUsed] =
    makeRequest[Vector[Price]](pricesRequest(instruments, Some(since)).withHeaders(env.headers))
      .log("prices").mapConcat(identity).log("price")


  def livePrices(accountID: Long, instruments: Seq[String], sessionId: Option[String] = None): Source[Xor[Price, Heartbeat], NotUsed] =
    startStreaming[Price](pricesStreamRequest(accountID, instruments, sessionId), "tick").log("price")

  /**
    * Get the last `count` historical candlesticks for an instrument.
    *
    * @param instrument   Name of the instrument to retrieve history for.
    *                     The value should be one of the available instrument codes from `instruments`.
    * @param count        The number of candlesticks to retrieve. If not specified, count will default to 500.
    *                     The maximum acceptable value for count is 5000.
    * @param includeFirst If it is set to “true”, the candlestick covered by the start timestamp will be returned.
    *                     If it is set to “false”, this candlestick will not be returned.
    *                     This field exists so clients may easily ensure that they can poll for all candles more recent
    *                     than their last received candle.
    * @param granularity  The time range represented by each candlestick. The default value is S5.
    * @param candleType   Determines the candlestick representation type.
    *                     This can by either `CandleTypes.BidAsk` or `CandleTypes.Midpoint`.
    * @return A source which emits the last `count` candlesticks for the requested instrument.
    */
  def candlesByCount[R](instrument: String, count: Int = 500, includeFirst: Boolean = true, granularity: CandleGranularity = S5, candleType: CandleTypes.Aux[R] = CandleTypes.BidAsk): Source[R, NotUsed] =
    makeRequest[Vector[candleType.R]](candlesRequest(instrument, Some(count), None, None, Some(includeFirst), granularity, candleType).withHeaders(env.headers))(candleType.decoder)
      .log("candles").mapConcat(identity).log("candle")

  /**
    * Get the historical candlesticks for an instrument in a date range.
    *
    * @param instrument   Name of the instrument to retrieve history for.
    *                     The value should be one of the available instrument codes from `instruments`.
    * @param startTime    The start timestamp for the range of candles requested.
    *                     The value specified must be in a valid datetime format.
    * @param endTime      The end timestamp for the range of candles requested.
    *                     The value specified must be in a valid datetime format.
    * @param includeFirst If it is set to “true”, the candlestick covered by the start timestamp will be returned.
    *                     If it is set to “false”, this candlestick will not be returned.
    *                     This field exists so clients may easily ensure that they can poll for all candles more recent
    *                     than their last received candle.
    * @param granularity  The time range represented by each candlestick. The default value is S5.
    * @param candleType   Determines the candlestick representation type.
    *                     This can by either `CandleTypes.BidAsk` or `CandleTypes.Midpoint`.
    * @return A source which emits all candlesticks in between the given time range for the requested instrument.
    */
  def candlesByDate[R](instrument: String, startTime: Long, endTime: Long, includeFirst: Boolean = true, granularity: CandleGranularity = S5, candleType: CandleTypes.Aux[R] = CandleTypes.BidAsk): Source[R, NotUsed] =
    makeRequest[Vector[candleType.R]](candlesRequest(instrument, None, Some(startTime), Some(endTime), Some(includeFirst), granularity, candleType).withHeaders(env.headers))(candleType.decoder)
      .log("candles").mapConcat(identity).log("candle")

}