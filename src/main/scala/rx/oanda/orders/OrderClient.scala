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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import rx.oanda.OandaEnvironment.ConnectionPool
import rx.oanda.events.OandaEvent
import rx.oanda.orders.OrderClient.OrderConfig
import rx.oanda.orders.OrderClientRequests._
import rx.oanda.utils.Side
import rx.oanda.{ApiConnection, OandaEnvironment}

class OrderClient(env: OandaEnvironment)(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool)
  extends ApiConnection {

  private[oanda] val apiConnection = env.apiFlow[Long]

  /**
    * This will return all pending orders for an account. Note: pending take profit or stop loss orders are recorded in the open trade object,
    * and will not be returned in this request.
    *
    * @param accountId   The account to request orders for.
    * @param instruments Retrieve open orders for a specific instrument only. Empty list means all instruments.
    * @param count       Maximum number of open orders to return. Default: 50. Max value: 500.
    * @param maxId       If specified, the server will return orders with id less than or equal to this, in descending order (for pagination).
    * @return A source which emits all orders for the specified instruments.
    */
  def orders(accountId: Long, instruments: Seq[String] = Nil, count: Int = 50, maxId: Option[Long] = None): Source[Order, NotUsed] =
    makeRequest[Vector[Order]](ordersRequest(accountId, instruments, Some(count), maxId, Nil).withHeaders(env.headers)).log("orders").mapConcat(identity).log("order")

  def createOrder(accountId: Long, orderConfig: OrderConfig): Source[MarketOrder, NotUsed] =
    makeRequest[MarketOrder](createOrderRequest(accountId, orderConfig).withHeaders(env.headers)).log("create-order")

  def ordersById(accountId: Long, orders: Seq[Long]): Source[Order, Unit] = ???

}
object OrderClient {

  case class OrderConfig(
    instrument: String,
    units: Int,
    side: Side,
    `type`: String,
    expiry: Option[Long] = None,
    price: Option[Double] = None,
    lowerBound: Option[Double] = None,
    upperBound: Option[Double] = None,
    stopLoss: Option[Double] = None,
    takeProfit: Option[Double] = None,
    trailingStop: Option[Double] = None) {

    def withExpiry(expiry: Long): OrderConfig = copy(expiry = Some(expiry))

    def withLowerBound(lowerBound: Double): OrderConfig = copy(lowerBound = Some(lowerBound))
    def withUpperBound(upperBound: Double): OrderConfig = copy(upperBound = Some(upperBound))

    def withTakeProfit(takeProfit: Double): OrderConfig = copy(takeProfit = Some(takeProfit))
    def withStopLoss(stopLoss: Double): OrderConfig = copy(stopLoss = Some(stopLoss))
    def withTrailingStop(trailingStop: Double): OrderConfig = copy(trailingStop = Some(trailingStop))

  }

  def marketOrder(instrument: String, units: Int, side: Side): OrderConfig = OrderConfig(instrument, units, side, "market")
  def limitOrder(instrument: String, units: Int, side: Side, expiry: Long, price: Double): OrderConfig = OrderConfig(instrument, units, side, "limit", expiry = Some(expiry), price = Some(price))
  def stopOrder(instrument: String, units: Int, side: Side, expiry: Long, price: Double): OrderConfig = OrderConfig(instrument, units, side, "stop", expiry = Some(expiry), price = Some(price))
  def marketIfTouchedOrder(instrument: String, units: Int, side: Side, expiry: Long, price: Double): OrderConfig = OrderConfig(instrument, units, side, "marketIfTouched", expiry = Some(expiry), price = Some(price))

}