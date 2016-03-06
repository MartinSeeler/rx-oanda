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
import io.circe.parser._
import org.scalatest._
import rx.oanda.utils._

class TradeClosedDecoderSpec extends FlatSpec with Matchers {

  behavior of "The TradeClosed Decoder"

  it must "parse a trade closed event from valid json" in {
    val json =
      """
        |{
        | "id": 54332,
        | "price": 1.30601,
        | "instrument": "EUR_USD",
        | "profit": 0.005,
        | "side": "sell",
        | "time": "1453282364000000"
        |}
      """.stripMargin
    decode[TradeClosed](json) should matchPattern {
      case Xor.Right(TradeClosed(54332, 1.30601, "EUR_USD", 0.005, Sell, 1453282364000000L)) ⇒
    }
  }

  it must "fail on missing property" in {
    val json =
      """
        |{
        | "id": 54332,
        | "price": 1.30601,
        | "instrument": "EUR_USD",
        | "profit": 0.005,
        | "side": "sell"
        |}
      """.stripMargin
    decode[TradeClosed](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

}
