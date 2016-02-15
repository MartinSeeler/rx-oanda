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

package rx.oanda.positions

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest._
import rx.oanda.utils._

class PositionDecoderSpec extends FlatSpec with Matchers {

  behavior of "The Position Decoder"

  it must "parse a position from valid json" in {
    val json =
      """
        |{
        |  "side" : "sell",
        |  "instrument" : "EUR_USD",
        |  "units" : 9,
        |  "avgPrice" : 1.3093
        |}
      """.stripMargin
    decode[Position](json) should matchPattern {
      case Xor.Right(Position(Sell, "EUR_USD", 9, 1.3093)) ⇒
    }
  }

  it must "fail when a property is missing" in {
    val json =
      """
        |{
        |  "side" : "sell",
        |  "units" : 9,
        |  "avgPrice" : 1.3093
        |}
      """.stripMargin
    decode[Position](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

  it must "parse a list of positions from valid json" in {
    val json =
      """
        |{
        | "positions" : [
        |  {
        |   "instrument" : "EUR_USD",
        |   "units" : 4741,
        |   "side" : "buy",
        |   "avgPrice" : 1.3626
        |  },
        |  {
        |   "instrument" : "USD_CAD",
        |   "units" : 30,
        |   "side" : "sell",
        |   "avgPrice" : 1.11563
        |  },
        |  {
        |   "instrument" : "USD_JPY",
        |   "units" : 88,
        |   "side" : "buy",
        |   "avgPrice" : 102.455
        |  }
        | ]
        |}
      """.stripMargin
    decode[Vector[Position]](json) should matchPattern {
      case Xor.Right(Vector(
        Position(Buy, "EUR_USD", 4741, 1.3626),
        Position(Sell, "USD_CAD", 30, 1.11563),
        Position(Buy, "USD_JPY", 88, 102.455)
      )) ⇒ //...
    }
  }

}
