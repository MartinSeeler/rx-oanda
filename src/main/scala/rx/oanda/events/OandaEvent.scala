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

package rx.oanda.events

import cats.data.Xor
import rx.oanda.utils.Side

import io.circe.{DecodingFailure, Decoder}
import io.circe.generic.semiauto._

trait OandaEvent {
  val id: Long
}

object OandaEvent {

  implicit val decodeOandaEvent: Decoder[OandaEvent] = Decoder.instance { c ⇒
    c.get[String]("type") flatMap {
      case "MARKET_ORDER_CREATE" ⇒ c.as[MarketOrderCreated]
      case "STOP_ORDER_CREATE" ⇒ c.as[StopOrderCreated]
      case "LIMIT_ORDER_CREATE" ⇒ c.as[LimitOrderCreated]
      case "MARKET_IF_TOUCHED_ORDER_CREATE" ⇒ c.as[MarketIfTouchedOrderCreated]
      case "ORDER_UPDATE" ⇒ c.as[OrderUpdated]
      case "ORDER_CANCEL" ⇒ c.as[OrderCanceled]
      case "ORDER_FILLED" ⇒ c.as[OrderFilled]
      case "TRADE_UPDATE" ⇒ c.as[TradeUpdated]
      case "TRADE_CLOSE" ⇒ c.as[TradeClosed]
      case "MIGRATE_TRADE_CLOSE" ⇒ c.as[MigrateTradeClosed]
      case "MIGRATE_TRADE_OPEN" ⇒ c.as[MigrateTradeOpened]
      case otherwise ⇒ Xor.left(DecodingFailure(s"Unknown OandaEvent of type $otherwise", c.history))
    }
  }

}

case class TradeOpened(id: Long, units: Int)
object TradeOpened {
  implicit val decodeTradeOpened: Decoder[TradeOpened] = deriveFor[TradeOpened].decoder
}
case class TradeReduced(id: Long, units: Int, pl: Double, interest: Double)
object TradeReduced {
  implicit val decodeTradeReduced: Decoder[TradeReduced] = deriveFor[TradeReduced].decoder
}

/**
  * A transaction of this type is created when a user has successfully traded a specified
  * number of units of an instrument at the current market price.
  */
case class MarketOrderCreated(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  side: Side,
  units: Int,
  price: Double,
  pl: Double,
  interest: Double,
  accountBalance: Double,
  lowerBound: Option[Double],
  upperBound: Option[Double],
  takeProfitPrice: Option[Double],
  stopLossPrice: Option[Double],
  trailingStopLossDistance: Option[Double],
  tradeOpened: Option[TradeOpened],
  tradeReduced: Option[TradeReduced]
) extends OandaEvent
object MarketOrderCreated {
  implicit val decodeMarketorderCreated: Decoder[MarketOrderCreated] = deriveFor[MarketOrderCreated].decoder
}

/**
  * A transaction of this type is created when a user has successfully placed a Stop order
  * on his/her account. A Stop order is an order which buys or sells a specified number of
  * units of an instrument when the market price for that instrument first becomes equal
  * to or worse than the price threshold specified.
  */
case class StopOrderCreated(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  side: Side,
  units: Int,
  price: Double,
  expiry: Long,
  reason: String,
  lowerBound: Option[Double],
  upperBound: Option[Double],
  takeProfitPrice: Option[Double],
  stopLossPrice: Option[Double],
  trailingStopLossDistance: Option[Double]
) extends OandaEvent
object StopOrderCreated {
  implicit val decodeStopOrderCreated: Decoder[StopOrderCreated] = deriveFor[StopOrderCreated].decoder
}

/**
  * A transaction of this type is created when a user has successfully placed a Limit order
  * on his/her account. A Limit order is an order which buys or sells a specified number of
  * units of an instrument when the market price for that instrument first becomes equal
  * to or better than the price threshold specified.
  */
case class LimitOrderCreated(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  side: Side,
  units: Int,
  price: Double,
  expiry: Long,
  reason: String,
  lowerBound: Option[Double],
  upperBound: Option[Double],
  takeProfitPrice: Option[Double],
  stopLossPrice: Option[Double],
  trailingStopLossDistance: Option[Double]
) extends OandaEvent
object LimitOrderCreated {
  implicit val decodeLimitOrderCreated: Decoder[LimitOrderCreated] = deriveFor[LimitOrderCreated].decoder
}

case class MarketIfTouchedOrderCreated(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  side: Side,
  units: Int,
  price: Double,
  expiry: Long,
  reason: String,
  lowerBound: Option[Double],
  upperBound: Option[Double],
  takeProfitPrice: Option[Double],
  stopLossPrice: Option[Double],
  trailingStopLossDistance: Option[Double]
) extends OandaEvent
object MarketIfTouchedOrderCreated {
  implicit val decodeMarketIfTouchedOrderCreated: Decoder[MarketIfTouchedOrderCreated] =
    deriveFor[MarketIfTouchedOrderCreated].decoder
}


case class OrderUpdated(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  units: Int,
  price: Double,
  orderId: Long,
  reason: String,
  lowerBound: Option[Double],
  upperBound: Option[Double],
  takeProfitPrice: Option[Double],
  stopLossPrice: Option[Double],
  trailingStopLossDistance: Option[Double]
) extends OandaEvent
object OrderUpdated {
  implicit val decodeOrderUpdated: Decoder[OrderUpdated] =
    deriveFor[OrderUpdated].decoder
}

case class OrderCanceled(
  id: Long,
  accountId: Long,
  time: Long,
  orderId: Long,
  reason: String
) extends OandaEvent
object OrderCanceled {
  implicit val decodeOrderCanceled: Decoder[OrderCanceled] =
    deriveFor[OrderCanceled].decoder
}

case class OrderFilled(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  units: Int,
  side: Side,
  price: Double,
  pl: Double,
  interest: Double,
  accountBalance: Double,
  orderId: Long,
  lowerBound: Option[Double],
  upperBound: Option[Double],
  takeProfitPrice: Option[Double],
  stopLossPrice: Option[Double],
  trailingStopLossDistance: Option[Double],
  tradeOpened: Option[TradeOpened],
  tradeReduced: Option[TradeReduced]
) extends OandaEvent
object OrderFilled {
  implicit val decodeOrderFilled: Decoder[OrderFilled] =
    deriveFor[OrderFilled].decoder
}

case class TradeUpdated(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  units: Int,
  side: Side,
  tradeId: Long,
  takeProfitPrice: Option[Double],
  stopLossPrice: Option[Double],
  trailingStopLossDistance: Option[Double]
) extends OandaEvent
object TradeUpdated {
  implicit val decodeTradeUpdated: Decoder[TradeUpdated] =
    deriveFor[TradeUpdated].decoder
}

case class TradeClosed(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  units: Int,
  side: Side,
  price: Double,
  pl: Double,
  interest: Double,
  accountBalance: Double,
  tradeId: Long
) extends OandaEvent
object TradeClosed {
  implicit val decodeTradeClosed: Decoder[TradeClosed] =
    deriveFor[TradeClosed].decoder
}

case class MigrateTradeClosed(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  units: Int,
  side: Side,
  price: Double,
  pl: Double,
  interest: Double,
  accountBalance: Double,
  tradeId: Long
) extends OandaEvent
object MigrateTradeClosed {
  implicit val decodeMigrateTradeClosed: Decoder[MigrateTradeClosed] =
    deriveFor[MigrateTradeClosed].decoder
}

/*
Required Fields: id, accountId, time, type, instrument, side, units, price

Optional Fields: takeProfitPrice, stopLossPrice, trailingStopLossDistance
 */
case class MigrateTradeOpened(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  units: Int,
  side: Side,
  price: Double,
  takeProfitPrice: Option[Double],
  stopLossPrice: Option[Double],
  trailingStopLossDistance: Option[Double],
  tradeOpened: TradeOpened
) extends OandaEvent
object MigrateTradeOpened {
  implicit val decodeMigrateTradeOpened: Decoder[MigrateTradeOpened] =
    deriveFor[MigrateTradeOpened].decoder
}