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

package rx.oanda.orders

import akka.http.scaladsl.model.HttpCharsets.`UTF-8`
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.MediaTypes.`application/x-www-form-urlencoded`
import akka.http.scaladsl.model._
import rx.oanda.utils.Side

private[orders] object OrderClientRequests {

  import rx.oanda.utils.QueryHelper._

  /**
    * Builds the request to return all pending orders for an account. Note: pending take profit or stop loss orders are recorded in the open trade object, and will not be returned in this request.
    *
    * @param accountId   The account to request orders for.
    * @param instruments Retrieve open orders for a specific instrument only. Default: all.
    * @param count       Maximum number of open orders to return. Default: 50. Max value: 500.
    * @param maxId       The server will return orders with id less than or equal to this, in descending order (for pagination).
    * @param ids         A list of orders to retrieve. Maximum number of ids: 50. No other parameter may be specified with the ids parameter.
    * @return The request to use without headers.
    */
  def ordersRequest(accountId: Long, instruments: Seq[String], count: Option[Int], maxId: Option[Long], ids: List[Long]): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/accounts/$accountId/orders").withRawQueryString(rawQueryStringOf(listParam("instruments", instruments) :: optionalParam("count", count) :: optionalParam("maxId", maxId) :: listParam("ids", ids) :: Nil)))

  /**
    * Builds the request to create a new order.
    *
    * @param accountId    The account to open the order on.
    * @param instrument   The instrument to open the order on.
    * @param units        The number of units to buy or sell.
    * @param side         The direction of the order, either ‘buy’ or ‘sell’.
    * @param `type`       The type of the order: ‘limit’, ‘stop’, ‘marketIfTouched’ or ‘market’.
    * @param expiry       If order type is ‘limit’, ‘stop’, or ‘marketIfTouched’.
    *                     The order expiration time in UTC. The value specified must be in a valid datetime format.
    * @param price        If order type is ‘limit’, ‘stop’, or ‘marketIfTouched’.
    *                     The price where the order is set to trigger at.
    * @param lowerBound   Optional the minimum execution price.
    * @param upperBound   Optional the maximum execution price.
    * @param stopLoss     Optional the stop loss price.
    * @param takeProfit   Optional the take profit price.
    * @param trailingStop Optional the trailing stop price.
    * @return The request to use, without headers.
    */
  def createOrderRequest(accountId: Long, instrument: String, units: Int, side: Side, `type`: String, expiry: Option[Long], price: Option[Double], lowerBound: Option[Double], upperBound: Option[Double], stopLoss: Option[Double], takeProfit: Option[Double], trailingStop: Option[Double]): HttpRequest =
    HttpRequest(POST, Uri(s"/v1/accounts/$accountId/orders"), entity = HttpEntity(rawQueryStringOf(
      param("instrument", instrument) :: param("units", units)
        :: param("type", `type`) :: param("side", side.toString.toLowerCase)
        :: optionalParam("expiry", expiry) :: optionalParam("price", price)
        :: optionalParam("lowerBound", lowerBound) :: optionalParam("upperBound", upperBound)
        :: optionalParam("stopLoss", stopLoss) :: optionalParam("takeProfit", takeProfit) :: optionalParam("trailingStop", trailingStop) :: Nil
    )).withContentType(ContentType(`application/x-www-form-urlencoded`, `UTF-8`)))

}
