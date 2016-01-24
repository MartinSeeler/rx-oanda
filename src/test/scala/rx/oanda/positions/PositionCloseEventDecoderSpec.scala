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
import io.circe.parse._
import org.scalatest._

class PositionCloseEventDecoderSpec extends FlatSpec with Matchers {

  behavior of "The PositionCloseEvent Decoder"

  it must "parse a position close event from valid json" in {
    val json =
      """
        |{
        | "ids" : [
        |   12345,
        |   12346,
        |   12347
        | ],
        | "instrument" : "EUR_USD",
        | "totalUnits": 1234,
        | "price" : 1.2345
        |}
      """.stripMargin
    decode[PositionCloseEvent](json) should matchPattern {
      case Xor.Right(PositionCloseEvent(Vector(12345, 12346, 12347), "EUR_USD", 1234, 1.2345)) ⇒
    }
  }

  it must "fail on missing property" in {
    val json =
      """
        |{
        | "ids" : [
        |   12345,
        |   12346,
        |   12347
        | ],
        | "totalUnits": 1234,
        | "price" : 1.2345
        |}
      """.stripMargin
    decode[PositionCloseEvent](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

}
