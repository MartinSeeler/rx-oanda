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

package rx.oanda.prices

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import cats.data.Xor
import rx.oanda.OandaEnvironment.ConnectionPool
import rx.oanda.prices.PricesClientRequests._
import rx.oanda.prices.candles.CandleGranularities.S5
import rx.oanda.prices.candles.{CandleGranularity, CandleTypes}
import rx.oanda.utils.Heartbeat
import rx.oanda.{ApiConnection, OandaEnvironment, StreamingConnection}

class PricesClient(env: OandaEnvironment)(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool)
  extends ApiConnection with StreamingConnection {

  private[oanda] val streamingConnection = env.streamFlow[Long]
  private[oanda] val apiConnection = env.apiFlow[Long]

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


  /**
    * Opens a streaming connection to receive real time market prices for specified instruments.
    *
    * @param accountId   The account that prices are applicable for.
    * @param instruments A list of instruments to fetch prices for.
    * @param sessionId   A unique session id used to identify the rate stream connection.
    *                    The value specified must be between 1 to 12 alphanumeric characters.
    *                    If a request is made with a session id that matches the session id of an
    *                    existing connection, the older connection will be disconnected.
    * @return A source which emits prices for all specified instruments as they arrive or `Heartbeat`s.
    */
  def livePrices(accountId: Long, instruments: Seq[String], sessionId: Option[String] = None): Source[Xor[Price, Heartbeat], NotUsed] =
    startStreaming[Price](pricesStreamRequest(accountId, instruments, sessionId).withHeaders(env.headers), "tick").log("price")

  /**
    * Get the last `count` historical candlesticks for an instrument.
    *
    * @param instrument   Name of the instrument to retrieve history for.
    *                     The value should be one of the available instrument codes from `instruments`.
    * @param count        The number of candlesticks to retrieve. If not specified, count will default to 500.
    *                     The maximum acceptable value for count is 5000.
    * @param granularity  The time range represented by each candlestick. The default value is S5.
    * @param candleType   Determines the candlestick representation type.
    *                     This can by either `CandleTypes.BidAsk` or `CandleTypes.Midpoint`.
    * @return A source which emits the last `count` candlesticks for the requested instrument.
    */
  def candlesByCount[R](instrument: String, count: Int = 500, granularity: CandleGranularity = S5, candleType: CandleTypes.Aux[R] = CandleTypes.BidAsk): Source[R, NotUsed] =
    makeRequest[Vector[candleType.R]](candlesRequest(instrument, Some(count), None, None, None, granularity, candleType).withHeaders(env.headers))(candleType.decoder)
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
