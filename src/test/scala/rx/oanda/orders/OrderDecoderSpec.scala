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
import io.circe.DecodingFailure
import io.circe.parse._
import org.scalatest._

class OrderDecoderSpec extends FlatSpec with Matchers {

  val json =
    """
      |{
      |  "id": 43211,
      |  "instrument": "EUR_USD",
      |  "units": 5,
      |  "side": "buy",
      |  "type": "limit",
      |  "time": "1453326442000000",
      |  "price": 1.45123,
      |  "takeProfit": 1.7,
      |  "stopLoss": 1.4,
      |  "expiry": "1453330035000000",
      |  "upperBound": 0,
      |  "lowerBound": 0,
      |  "trailingStop": 10
      |}
    """.stripMargin

  val ordersJson =
    """
      |{
      | "orders" : [
      |  {
      |   "id" : 175427639,
      |   "instrument" : "EUR_USD",
      |   "units" : 20,
      |   "side" : "buy",
      |   "type" : "marketIfTouched",
      |   "time" : 1453326442000000,
      |   "price" : 1,
      |   "takeProfit" : 0,
      |   "stopLoss" : 0,
      |   "expiry" : 1453330035000000,
      |   "upperBound" : 0,
      |   "lowerBound" : 0,
      |   "trailingStop" : 0
      |  },
      |  {
      |   "id" : 175427637,
      |   "instrument" : "EUR_USD",
      |   "units" : 10,
      |   "side" : "sell",
      |   "type" : "marketIfTouched",
      |   "time" : 1453326442000000,
      |   "price" : 1,
      |   "takeProfit" : 0,
      |   "stopLoss" : 0,
      |   "expiry" : 1453330035000000,
      |   "upperBound" : 0,
      |   "lowerBound" : 0,
      |   "trailingStop" : 0
      |  }
      | ]
      |}
    """.stripMargin

  behavior of "The Order Decoder"

  it must "parse an order from valid json" in {
    val json =
      """
        |{
        |  "id": 43211,
        |  "instrument": "EUR_USD",
        |  "units": 5,
        |  "side": "buy",
        |  "type": "limit",
        |  "time": "1453326442000000",
        |  "price": 1.45123,
        |  "takeProfit": 1.7,
        |  "stopLoss": 1.4,
        |  "expiry": "1453330035000000",
        |  "upperBound": 0,
        |  "lowerBound": 0,
        |  "trailingStop": 10
        |}
      """.stripMargin

    decode[Order](json) should matchPattern {
      case Xor.Right(Order(43211, "EUR_USD", 5, "buy", "limit", 1453326442000000L, 1.45123, 1.7, 1.4, 1453330035000000L, 0, 0, 10)) ⇒
    }
  }

  it must "fail on missing property" in {
    val json =
      """
        |{
        |  "id": 43211,
        |  "instrument": "EUR_USD",
        |  "units": 5,
        |  "side": "buy",
        |  "type": "limit",
        |  "price": 1.45123,
        |  "takeProfit": 1.7,
        |  "stopLoss": 1.4,
        |  "expiry": "1453330035000000",
        |  "upperBound": 0,
        |  "lowerBound": 0,
        |  "trailingStop": 10
        |}
      """.stripMargin

    decode[Order](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

  it must "parse the orders list from valid json" in {
    val json =
      """
        |{
        | "orders" : [
        |  {
        |   "id" : 175427639,
        |   "instrument" : "EUR_USD",
        |   "units" : 20,
        |   "side" : "buy",
        |   "type" : "marketIfTouched",
        |   "time" : 1453326442000000,
        |   "price" : 1,
        |   "takeProfit" : 0,
        |   "stopLoss" : 0,
        |   "expiry" : 1453330035000000,
        |   "upperBound" : 0,
        |   "lowerBound" : 0,
        |   "trailingStop" : 0
        |  },
        |  {
        |   "id": 43211,
        |   "instrument": "EUR_CAD",
        |   "units": 5,
        |   "side": "buy",
        |   "type": "limit",
        |   "time": "1453326442000000",
        |   "price": 1.45123,
        |   "takeProfit": 1.7,
        |   "stopLoss": 1.4,
        |   "expiry": "1453330035000000",
        |   "upperBound": 0,
        |   "lowerBound": 0,
        |   "trailingStop": 10
        |  }
        | ]
        |}
      """.stripMargin

    decode[Vector[Order]](json) should matchPattern {
      case Xor.Right(Vector(
      Order(175427639, "EUR_USD", 20, "buy", "marketIfTouched", 1453326442000000L, 1.0, 0, 0, 1453330035000000L, 0, 0, 0),
      Order(43211, "EUR_CAD", 5, "buy", "limit", 1453326442000000L, 1.45123, 1.7, 1.4, 1453330035000000L, 0, 0, 10)
      )) ⇒ //...
    }
  }

}
