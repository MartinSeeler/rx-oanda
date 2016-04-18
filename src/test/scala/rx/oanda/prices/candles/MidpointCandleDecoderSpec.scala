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

package rx.oanda.prices.candles

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest.{FlatSpec, Matchers}

class MidpointCandleDecoderSpec extends FlatSpec with Matchers {

  behavior of "The MidpointCandle Decoder"

  it must "parse a MidpointCandle from valid json" in {
    val json =
      """
        |{
        | "time": "1455488785000000",
        | "openMid": 1.2,
        | "highMid": 1.4,
        | "lowMid": 1.0,
        | "closeMid": 1.1,
        | "volume": 1337,
        | "complete": true
        |}
      """.stripMargin
    decode[MidpointCandle](json) should matchPattern {
      case Xor.Right(MidpointCandle(1455488785000000L, 1.2, 1.4, 1.0, 1.1, 1337, true)) ⇒
    }
  }

  it must "fail to parse a MidpointCandle when a property is missing" in {
    val json =
      """
        |{
        | "time": "1455488785000000",
        | "openMid": 1.2,
        | "highMid": 1.4,
        | "closeMid": 1.1,
        | "volume": 1337,
        | "complete": true
        |}
      """.stripMargin
    decode[MidpointCandle](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

  it must "parse a list of MidpointCandle's from valid json" in {
    val json =
      """
        |{
        |  "candles": [
        |    {
        |      "time": "1455488785000000",
        |      "openMid": 1.2,
        |      "highMid": 1.4,
        |      "lowMid": 1.0,
        |      "closeMid": 1.1,
        |      "volume": 1337,
        |      "complete": true
        |    },
        |    {
        |      "time": "1455488788000000",
        |      "openMid": 1.3,
        |      "highMid": 1.5,
        |      "lowMid": 1.1,
        |      "closeMid": 1.2,
        |      "volume": 42,
        |      "complete": true
        |    }
        |  ]
        |}
      """.stripMargin
    decode[Vector[MidpointCandle]](json) should matchPattern {
      case Xor.Right(Vector(MidpointCandle(1455488785000000L, 1.2, 1.4, 1.0, 1.1, 1337, true), MidpointCandle(1455488788000000L, 1.3, 1.5, 1.1, 1.2, 42, true))) ⇒
    }
  }

}
