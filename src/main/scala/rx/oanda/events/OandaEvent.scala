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