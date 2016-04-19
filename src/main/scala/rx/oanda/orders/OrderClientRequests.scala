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
import rx.oanda.orders.OrderClient.OrderConfig
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
    * Builds the request to close a single order by it's id.
    *
    * @param accountId The account where to order was placed.
    * @param orderId   The id of the order to close.
    * @return The request to use without headers.
    */
  def closeOrderRequest(accountId: Long, orderId: Long): HttpRequest =
    HttpRequest(DELETE, Uri(s"/v1/accounts/$accountId/orders/$orderId"))

  /**
    * Builds the request to create a new order.
    *
    * @param accountId   The account to open the order on.
    * @param orderConfig The configuration settings for this order with all necessary values.
    * @return The request to use, without headers.
    */
  def createOrderRequest(accountId: Long, orderConfig: OrderConfig): HttpRequest =
    HttpRequest(POST, Uri(s"/v1/accounts/$accountId/orders"), entity = HttpEntity(rawQueryStringOf(
      param("instrument", orderConfig.instrument) :: param("units", orderConfig.units)
        :: param("type", orderConfig.`type`) :: param("side", orderConfig.side.toString.toLowerCase)
        :: optionalParam("expiry", orderConfig.expiry) :: optionalParam("price", orderConfig.price)
        :: optionalParam("lowerBound", orderConfig.lowerBound) :: optionalParam("upperBound", orderConfig.upperBound)
        :: optionalParam("stopLoss", orderConfig.stopLoss) :: optionalParam("takeProfit", orderConfig.takeProfit) :: optionalParam("trailingStop", orderConfig.trailingStop) :: Nil
    )).withContentType(ContentType(`application/x-www-form-urlencoded`, `UTF-8`)))

}
