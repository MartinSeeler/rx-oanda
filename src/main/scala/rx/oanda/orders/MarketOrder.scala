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

import cats.data.Xor
import io.circe.Decoder
import io.circe.generic.semiauto._
import rx.oanda.utils.Side

case class MarketOrder(
  instrument: String,
  time: Long,
  price: Double,
  tradeOpened: Option[MarketOrder.TradeOpened],
  tradesClosed: List[MarketOrder.TradeClosed],
  tradeReduced: Option[MarketOrder.TradeClosed]
)

object MarketOrder {

  implicit def decodeOption[A](implicit A: Decoder[A]): Decoder[Option[A]] = Decoder.instance { c ⇒
    if (c.focus.asObject.exists(_.isEmpty))
      Xor.right(None)
    else
      Decoder.decodeOption(A).apply(c)
  }

  case class TradeOpened(
    id: Long,
    units: Int,
    side: Side,
    takeProfit: Option[Double],
    stopLoss: Option[Double],
    trailingStop: Option[Double]
  )

  object TradeOpened {

    implicit val decodeDoubleOption: Decoder[Option[Double]] = Decoder.decodeDouble.map{
      case 0.0 ⇒ None
      case x ⇒ Some(x)
    }

    implicit val decodeTradeOpened: Decoder[MarketOrder.TradeOpened] = deriveDecoder
  }

  case class TradeClosed(id: Long, units: Int, side: Side)

  object TradeClosed {
    implicit val decodeTradeClosed: Decoder[MarketOrder.TradeClosed] = deriveDecoder
  }

  implicit val decodeMarketOrder: Decoder[MarketOrder] = deriveDecoder

}
