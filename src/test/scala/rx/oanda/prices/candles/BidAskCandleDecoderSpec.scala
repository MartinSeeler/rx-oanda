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

class BidAskCandleDecoderSpec extends FlatSpec with Matchers {

  behavior of "The BidAskCandle Decoder"

  it must "parse a BidAskCandle from valid json" in {
    val json =
      """
        |{
        | "time": "1455488785000000",
        | "openBid": 1.1001,
        | "openAsk": 1.1005,
        | "highBid": 1.202,
        | "highAsk": 1.204,
        | "lowBid": 1.0001,
        | "lowAsk": 1.0005,
        | "closeBid": 1.1101,
        | "closeAsk": 1.1105,
        | "volume": 1337,
        | "complete": true
        |}
      """.stripMargin
    decode[BidAskCandle](json) should matchPattern {
      case Xor.Right(BidAskCandle(1455488785000000L, 1.1001, 1.1005, 1.202, 1.204, 1.0001, 1.0005, 1.1101, 1.1105, 1337, true)) ⇒
    }
  }

  it must "fail to parse a BidAskCandle when a property is missing" in {
    val json =
      """
        |{
        | "time": "1455488785000000",
        | "openBid": 1.1001,
        | "openAsk": 1.1005,
        | "highAsk": 1.204,
        | "lowBid": 1.0001,
        | "lowAsk": 1.0005,
        | "closeBid": 1.1101,
        | "closeAsk": 1.1105,
        | "volume": 1337,
        | "complete": true
        |}
      """.stripMargin
    decode[BidAskCandle](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

  it must "parse a list of BidAskCandle's from valid json" in {
    val json =
      """
        |{
        |  "candles": [
        |    {
        |      "time": "1455488785000000",
        |      "openBid": 1.1001,
        |      "openAsk": 1.1005,
        |      "highBid": 1.202,
        |      "highAsk": 1.204,
        |      "lowBid": 1.0001,
        |      "lowAsk": 1.0005,
        |      "closeBid": 1.1101,
        |      "closeAsk": 1.1105,
        |      "volume": 1337,
        |      "complete": true
        |    },
        |    {
        |      "time": "1455488788000000",
        |      "openBid": 1.2001,
        |      "openAsk": 1.2005,
        |      "highBid": 1.302,
        |      "highAsk": 1.304,
        |      "lowBid": 1.1001,
        |      "lowAsk": 1.1005,
        |      "closeBid": 1.2101,
        |      "closeAsk": 1.2105,
        |      "volume": 42,
        |      "complete": true
        |    }
        |  ]
        |}
      """.stripMargin
    decode[Vector[BidAskCandle]](json) should matchPattern {
      case Xor.Right(Vector(BidAskCandle(1455488785000000L, 1.1001, 1.1005, 1.202, 1.204, 1.0001, 1.0005, 1.1101, 1.1105, 1337, true), BidAskCandle(1455488788000000L, 1.2001, 1.2005, 1.302, 1.304, 1.1001, 1.1005, 1.2101, 1.2105, 42, true))) ⇒
    }
  }

}
