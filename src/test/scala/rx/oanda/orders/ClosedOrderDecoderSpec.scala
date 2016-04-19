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
import io.circe.parser._
import org.scalatest._
import rx.oanda.utils.Buy

class ClosedOrderDecoderSpec extends FlatSpec with Matchers {

  behavior of "The ClosedOrder Decoder"

  it must "parse a closed order from valid json" in {
    val json =
      """
        |{
        |  "id": 10234643940,
        |  "instrument": "EUR_USD",
        |  "price": 1.29343,
        |  "side": "buy",
        |  "time": "1453326442000000",
        |  "type": "BuyEntry",
        |  "units": 1
        |}
      """.stripMargin

    decode[ClosedOrder](json) should matchPattern {
      case Xor.Right(ClosedOrder(10234643940L, "EUR_USD", 1.29343, Buy, 1453326442000000L, 1)) ⇒
    }
  }

  it must "fail on missing property" in {
    val json =
      """
        |{
        |  "id": 10234643940,
        |  "instrument": "EUR_USD",
        |  "side": "buy",
        |  "time": "1453326442000000",
        |  "type": "BuyEntry",
        |  "units": 1
        |}
      """.stripMargin

    decode[ClosedOrder](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

}
