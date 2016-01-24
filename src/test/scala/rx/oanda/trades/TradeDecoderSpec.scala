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

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parse._
import org.scalatest._
import rx.oanda.orders.{Buy, Sell}

class TradeDecoderSpec extends FlatSpec with Matchers {

  behavior of "The Trade Decoder"

  it must "parse a trade from valid json" in {
    val json =
      """
        |{
        | "id": 43211,
        | "units": 5,
        | "side": "buy",
        | "instrument": "EUR_USD",
        | "time": "1453282364000000",
        | "price": 1.45123,
        | "takeProfit": 1.7,
        | "stopLoss": 1.4,
        | "trailingStop": 50,
        | "trailingAmount": 1.44613
        |}
      """.stripMargin
    decode[Trade](json) should matchPattern {
      case Xor.Right(Trade(43211, 5, Buy, "EUR_USD", 1453282364000000L, 1.45123, 1.7, 1.4, 50, 1.44613)) ⇒
    }
  }

  it must "fail on missing property" in {
    val json =
      """
        |{
        | "id": 43211,
        | "units": 5,
        | "side": "buy",
        | "instrument": "EUR_USD",
        | "time": "1453282364000000",
        | "takeProfit": 1.7,
        | "stopLoss": 1.4,
        | "trailingStop": 50,
        | "trailingAmount": 1.44613
        |}
      """.stripMargin
    decode[Trade](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

  it must "parse the trades list from valid json" in {
    val json =
      """
        |{
        | "trades" : [
        |  {
        |   "id" : 175427743,
        |   "units" : 2,
        |   "side" : "sell",
        |   "instrument" : "EUR_USD",
        |   "time" : "1453282364000000",
        |   "price" : 1.36687,
        |   "takeProfit" : 0,
        |   "stopLoss" : 0,
        |   "trailingStop" : 0,
        |   "trailingAmount": 0
        |  },
        |  {
        |   "id" : 175427742,
        |   "units" : 2,
        |   "side" : "buy",
        |   "instrument" : "EUR_USD",
        |   "time" : "1453282364000000",
        |   "price" : 1.36687,
        |   "takeProfit" : 0,
        |   "stopLoss" : 0,
        |   "trailingStop" : 1.37,
        |   "trailingAmount" : 0
        |  }
        | ]
        |}
      """.stripMargin

    decode[Vector[Trade]](json) should matchPattern {
      case Xor.Right(Vector(
        Trade(175427743, 2, Sell, "EUR_USD", 1453282364000000L, 1.36687, 0, 0, 0, 0),
        Trade(175427742, 2, Buy, "EUR_USD", 1453282364000000L, 1.36687, 0, 0, 1.37, 0)
      )) ⇒ //...
    }
  }

}
