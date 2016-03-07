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
import io.circe.parser._
import org.scalatest.{FlatSpec, Matchers}
import rx.oanda.orders.MarketOrder.{TradeClosed, TradeOpened}
import rx.oanda.utils.{Buy, Sell}

class MarketOrderDecoderSpec extends FlatSpec with Matchers {

  behavior of "The MarketOrder Decoder"

  it must "parse a market order with one opened trade from valid json" in {
    val json =
      """
        |{
        |  "instrument": "EUR_USD",
        |  "time": "1455612600000000",
        |  "price": 1.37041,
        |  "tradeOpened": {
        |    "id": 175517237,
        |    "units": 2,
        |    "side": "sell",
        |    "takeProfit": 0,
        |    "stopLoss": 13,
        |    "trailingStop": 0
        |  },
        |  "tradesClosed": [],
        |  "tradeReduced": {}
        |}
      """.stripMargin
    decode[MarketOrder](json) should matchPattern {
      case Xor.Right(MarketOrder("EUR_USD", 1455612600000000L, 1.37041, Some(TradeOpened(175517237L, 2, Sell, None, Some(13), None)), Nil, None)) ⇒
    }
  }

  it must "parse a market order with one opened trade and some trades closed from valid json" in {
    val json =
      """
        |{
        |  "instrument" : "EUR_USD",
        |  "time" : "1456433140000000",
        |  "price" : 1.10269,
        |  "tradeOpened" : {
        |    "id" : 10132104801,
        |    "units" : 5,
        |    "side" : "sell",
        |    "takeProfit" : 0,
        |    "stopLoss" : 0,
        |    "trailingStop" : 10
        |  },
        |  "tradesClosed" : [
        |    {
        |      "id" : 10132024832,
        |      "units" : 1,
        |      "side" : "buy"
        |    }
        |  ],
        |  "tradeReduced" : {}
        |}
      """.stripMargin
    decode[MarketOrder](json) should matchPattern {
      case Xor.Right(MarketOrder("EUR_USD", 1456433140000000L, 1.10269, Some(TradeOpened(10132104801L, 5, Sell, None, None, Some(10))), TradeClosed(10132024832L, 1, Buy) :: Nil, None)) ⇒
    }
  }

  it must "parse a market order with a reduced trade from valid json" in {
    val json =
      """
        |{
        |  "instrument" : "EUR_USD",
        |  "time" : "1456434001000000",
        |  "price" : 1.1025,
        |  "tradeOpened" : {},
        |  "tradesClosed" : [],
        |  "tradeReduced" : {
        |    "id" : 10132127667,
        |    "units" : 4,
        |    "side" : "sell"
        |  }
        |}
      """.stripMargin
    decode[MarketOrder](json) should matchPattern {
      case Xor.Right(MarketOrder("EUR_USD", 1456434001000000L, 1.1025, None, Nil, Some(TradeClosed(10132127667L, 4, Sell)))) ⇒
    }
  }

}
