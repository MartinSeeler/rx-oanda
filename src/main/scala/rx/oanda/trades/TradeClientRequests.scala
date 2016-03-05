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

package rx.oanda.trades

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpRequest, Uri}
import rx.oanda.utils.QueryHelper._

private[trades] object TradeClientRequests {

  /**
    * Builds the request to get a list of open trades
    *
    * @param accountId  The account id to use.
    * @param maxId      Optional The server will return trades with id less than or equal to this,
    *                   in descending order (for pagination).
    * @param count      Optional Maximum number of open trades to return. Default: 50 Max value: 500
    * @param instrument Optional Retrieve open trades for a specific instrument only Default: all
    * @param ids        Optional A list of ids of specific trades to retrieve.
    *                   Maximum number of ids: 50. No other parameter may be specified with the ids parameter.
    * @return The request to use without headers.
    */
  def tradesRequest(accountId: Long, maxId: Option[Long], count: Option[Int], instrument: Option[String], ids: Seq[Long]): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/accounts/$accountId/trades").withRawQueryString(rawQueryStringOf(optionalParam("maxId", maxId) :: optionalParam("count", count) :: optionalParam("instrument", instrument) :: listParam("ids", ids) :: Nil)))

  /**
    * Builds the request to get a specific trade.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to retrieve.
    * @return The request to use without headers.
    */
  def getTradeRequest(accountId: Long, tradeId: Long): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/accounts/$accountId/trades/$tradeId"))

  /**
    * Builds the request to modify a specifc trade. TO remove any of the
    * optional parameters, set it's value to `0`.
    *
    * @param accountId    The account id to use.
    * @param tradeId      The id of the trade to modify.
    * @param stopLoss     Optional Stop Loss value.
    * @param takeProfit   Optional Take Profit value.
    * @param trailingStop Optional Trailing Stop distance in pips, up to one decimal place.
    * @return The request to use, without headers.
    */
  def modifyTradeRequest(accountId: Long, tradeId: Long, stopLoss: Option[Double], takeProfit: Option[Double], trailingStop: Option[Double]): HttpRequest =
    HttpRequest(PATCH, Uri(s"/v1/accounts/$accountId/trades/$tradeId"), entity = HttpEntity(rawQueryStringOf(
      optionalParam("stopLoss", stopLoss) :: optionalParam("takeProfit", takeProfit) :: optionalParam("trailingStop", trailingStop) :: Nil
    )).withContentType(ContentType(`application/x-www-form-urlencoded`, `UTF-8`)))

  /**
    * Builds the request to close a specific trade.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to close.
    * @return The request to use without headers.
    */
  def closeTradeRequest(accountId: Long, tradeId: Long): HttpRequest =
    HttpRequest(DELETE, Uri(s"/v1/accounts/$accountId/trades/$tradeId"))
}
