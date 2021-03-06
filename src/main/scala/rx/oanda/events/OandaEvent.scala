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
import io.circe.generic.semiauto._
import io.circe.{Decoder, DecodingFailure}
import rx.oanda.utils.Side

trait OandaEvent {
  val id: Long
  val accountId: Long
  val time: Long
}

trait InstrumentEvent {
  val instrument: String
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
      case "TAKE_PROFIT_FILLED" ⇒ c.as[TakeProfitFilled]
      case "STOP_LOSS_FILLED" ⇒ c.as[StopLossFilled]
      case "TRAILING_STOP_FILLED" ⇒ c.as[TrailingStopFilled]
      case "MARGIN_CALL_ENTER" ⇒ c.as[MarginCallEntered]
      case "MARGIN_CALL_EXIT" ⇒ c.as[MarginCallExited]
      case "MARGIN_CLOSEOUT" ⇒ c.as[MarginCloseoutTriggered]
      case "SET_MARGIN_RATE" ⇒ c.as[MarginRateChanged]
      case "TRANSFER_FUNDS" ⇒ c.as[FundsTransfered]
      case "DAILY_INTEREST" ⇒ c.as[DailyInterest]
      case "FEE" ⇒ c.as[Fee]
      case otherwise ⇒ Xor.left(DecodingFailure(s"Unknown OandaEvent of type $otherwise", c.history))
    }
  }

}

case class TradeOpened(id: Long, units: Int)

object TradeOpened {
  implicit val decodeTradeOpened: Decoder[TradeOpened] = deriveDecoder
}

case class TradeReduced(id: Long, units: Int, pl: Double, interest: Double)

object TradeReduced {
  implicit val decodeTradeReduced: Decoder[TradeReduced] = deriveDecoder
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
) extends OandaEvent with InstrumentEvent

object MarketOrderCreated {
  implicit val decodeMarketorderCreated: Decoder[MarketOrderCreated] = deriveDecoder
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
) extends OandaEvent with InstrumentEvent

object StopOrderCreated {
  implicit val decodeStopOrderCreated: Decoder[StopOrderCreated] = deriveDecoder
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
) extends OandaEvent with InstrumentEvent

object LimitOrderCreated {
  implicit val decodeLimitOrderCreated: Decoder[LimitOrderCreated] = deriveDecoder
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
) extends OandaEvent with InstrumentEvent

object MarketIfTouchedOrderCreated {
  implicit val decodeMarketIfTouchedOrderCreated: Decoder[MarketIfTouchedOrderCreated] = deriveDecoder
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
) extends OandaEvent with InstrumentEvent

object OrderUpdated {
  implicit val decodeOrderUpdated: Decoder[OrderUpdated] = deriveDecoder
}

case class OrderCanceled(
  id: Long,
  accountId: Long,
  time: Long,
  orderId: Long,
  reason: String
) extends OandaEvent

object OrderCanceled {
  implicit val decodeOrderCanceled: Decoder[OrderCanceled] = deriveDecoder
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
) extends OandaEvent with InstrumentEvent

object OrderFilled {
  implicit val decodeOrderFilled: Decoder[OrderFilled] = deriveDecoder
}

case class TradeUpdated(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  units: Int,
  tradeId: Long,
  takeProfitPrice: Option[Double],
  stopLossPrice: Option[Double],
  trailingStopLossDistance: Option[Double]
) extends OandaEvent with InstrumentEvent

object TradeUpdated {
  implicit val decodeTradeUpdated: Decoder[TradeUpdated] = deriveDecoder
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
) extends OandaEvent with InstrumentEvent

object TradeClosed {
  implicit val decodeTradeClosed: Decoder[TradeClosed] = deriveDecoder
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
) extends OandaEvent with InstrumentEvent

object MigrateTradeClosed {
  implicit val decodeMigrateTradeClosed: Decoder[MigrateTradeClosed] = deriveDecoder
}

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
) extends OandaEvent with InstrumentEvent

object MigrateTradeOpened {
  implicit val decodeMigrateTradeOpened: Decoder[MigrateTradeOpened] = deriveDecoder
}

case class TakeProfitFilled(
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
) extends OandaEvent with InstrumentEvent

object TakeProfitFilled {
  implicit val decodeTakeProfitFilled: Decoder[TakeProfitFilled] = deriveDecoder
}

case class StopLossFilled(
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
) extends OandaEvent with InstrumentEvent

object StopLossFilled {
  implicit val decodeStopLossFilled: Decoder[StopLossFilled] = deriveDecoder
}

case class TrailingStopFilled(
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
) extends OandaEvent with InstrumentEvent

object TrailingStopFilled {
  implicit val decodeTrailingStopFilled: Decoder[TrailingStopFilled] = deriveDecoder
}

case class MarginCallEntered(
  id: Long,
  accountId: Long,
  time: Long
) extends OandaEvent

object MarginCallEntered {
  implicit val decodeMarginCallEntered: Decoder[MarginCallEntered] = deriveDecoder
}

case class MarginCallExited(
  id: Long,
  accountId: Long,
  time: Long
) extends OandaEvent

object MarginCallExited {
  implicit val decodeMarginCallExited: Decoder[MarginCallExited] = deriveDecoder
}

case class MarginCloseoutTriggered(
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
) extends OandaEvent with InstrumentEvent

object MarginCloseoutTriggered {
  implicit val decodeMarginCloseoutTriggered: Decoder[MarginCloseoutTriggered] = deriveDecoder
}

case class MarginRateChanged(
  id: Long,
  accountId: Long,
  time: Long,
  rate: Double
) extends OandaEvent

object MarginRateChanged {
  implicit val decodeMarginRateChanged: Decoder[MarginRateChanged] = deriveDecoder
}

case class FundsTransfered(
  id: Long,
  accountId: Long,
  time: Long,
  amount: Double,
  accountBalance: Double,
  reason: String
) extends OandaEvent

object FundsTransfered {
  implicit val decodeFundsTransfered: Decoder[FundsTransfered] = deriveDecoder
}

case class DailyInterest(
  id: Long,
  accountId: Long,
  time: Long,
  instrument: String,
  interest: Double,
  accountBalance: Double
) extends OandaEvent with InstrumentEvent

object DailyInterest {
  implicit val decodeDailyInterest: Decoder[DailyInterest] = deriveDecoder
}

case class Fee(
  id: Long,
  accountId: Long,
  time: Long,
  amount: Double,
  accountBalance: Double,
  reason: String
) extends OandaEvent

object Fee {
  implicit val decodeFee: Decoder[Fee] = deriveDecoder
}
