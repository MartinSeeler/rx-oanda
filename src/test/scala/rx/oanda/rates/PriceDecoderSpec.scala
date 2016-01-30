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

package rx.oanda.rates

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parse._
import org.scalatest._

class PriceDecoderSpec extends FlatSpec with Matchers {

  behavior of "The Price Decoder"

  it must "parse a price from valid json" in {
    val json =
      """
        |{
        | "instrument": "EUR_USD",
        | "time": "1453847424597195",
        | "bid": 1.08646,
        | "ask": 1.08668
        |}
      """.stripMargin
    decode[Price](json) should matchPattern {
      case Xor.Right(Price("EUR_USD", 1453847424597195L, 1.08646, 1.08668)) ⇒
    }
  }

  it must "fail when a property is missing" in {
    val json =
      """
        |{
        | "instrument": "EUR_USD",
        | "time": "1453847424597195",
        | "ask": 1.08668
        |}
      """.stripMargin
    decode[Price](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

  it must "parse a list of prices from valid json" in {
    val json =
      """
        |{
        | "prices": [
        |  {
        |   "instrument": "EUR_USD",
        |   "time": "1453847424597195",
        |   "bid": 1.08646,
        |   "ask": 1.08668
        |  }
        | ]
        |}
      """.stripMargin
    decode[Vector[Price]](json) should matchPattern {
      case Xor.Right(Vector(
        Price("EUR_USD", 1453847424597195L, 1.08646, 1.08668)
      )) ⇒
    }
  }

}
