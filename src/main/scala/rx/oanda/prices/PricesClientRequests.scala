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

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, Uri}
import rx.oanda.prices.candles.{CandleGranularity, CandleTypes}
import rx.oanda.utils.QueryHelper._

private[prices] object PricesClientRequests {

  /**
    * Builds the request to fetch live prices for specified instruments.
    *
    * @param instruments A list of instruments to fetch prices for.
    *                    Values should be one of the available instrument from `instruments`.
    * @param since       Optional when specified, only prices that occurred after the specified timestamp are returned.
    *                    The value specified must be in a valid datetime format.
    * @return The request to use without headers.
    */
  def pricesRequest(instruments: Seq[String], since: Option[Long]): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/prices").withRawQueryString(rawQueryStringOf(listParam("instruments", instruments) :: optionalParam("since", since) :: Nil)))

  /**
    * Builds the request to get historical information for an instrument.
    *
    * @param instrument   Name of the instrument to retrieve history for.
    *                     The value should be one of the available instrument from `instruments`.
    * @param count        Optional the number of candlesticks to retrieve. If not specified, count will default to 500.
    *                     The maximum acceptable value for count is 5000.
    * @param startTime    Optional the start timestamp for the range of candles requested.
    *                     The value specified must be in a valid datetime format.
    * @param endTime      Optional the end timestamp for the range of candles requested.
    *                     The value specified must be in a valid datetime format.
    * @param includeFirst An optional boolean field which may be set to “true” or “false”.
    *                     If it is set to “true”, the candlestick covered by the start timestamp will be returned.
    *                     If it is set to “false”, this candlestick will not be returned.
    *                     This field exists so clients may easily ensure that they can poll for all candles more recent
    *                     than their last received candle.
    * @param granularity  The time range represented by each candlestick.
    * @param candleType   Determines the candlestick representation type.
    *                     This can by either `CandleTypes.BidAsk` or `CandleTypes.Midpoint`.
    * @return The request to use without headers.
    */
  def candlesRequest[R](instrument: String, count: Option[Int], startTime: Option[Long], endTime: Option[Long], includeFirst: Option[Boolean], granularity: CandleGranularity, candleType: CandleTypes.Aux[R]): HttpRequest =
    HttpRequest(GET, Uri("/v1/candles").withRawQueryString(rawQueryStringOf(
      param("instrument", instrument) :: optionalParam("count", count) ::
        optionalParam("start", startTime) :: optionalParam("end", endTime) ::
        param("granularity", granularity.toString) :: param("candleFormat", candleType.uriParam) ::
        optionalParam("includeFirst", includeFirst) :: Nil
    )))

  /**
    * Builds the request to open a streaming connection to receive real time market prices for specified instruments.
    *
    * @param accountId   The account that prices are applicable for.
    * @param instruments A list of instruments to fetch prices for.
    * @param sessionId   A unique session id used to identify the rate stream connection.
    *                    The value specified must be between 1 to 12 alphanumeric characters.
    *                    If a request is made with a session id that matches the session id of an
    *                    existing connection, the older connection will be disconnected.
    * @return The request to use wihtout headers.
    */
  def pricesStreamRequest(accountId: Long, instruments: Seq[String], sessionId: Option[String]): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/prices").withRawQueryString(rawQueryStringOf(param("accountId", accountId) :: listParam("instruments", instruments) :: optionalParam("sessionId", sessionId) :: Nil)))

}
