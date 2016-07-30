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

package rx.oanda.order

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest.{FlatSpec, Matchers}

class OrderStateDecoderSpec extends FlatSpec with Matchers {

  behavior of "The OrderState Decoder"

  it can "parse a pending order state from valid json" in {
    val json =
      """
        |"PENDING"
      """.stripMargin
    decode[OrderState](json) should matchPattern {
      case Xor.Right(Pending) => //
    }
  }

  it can "parse a filled order state from valid json" in {
    val json =
      """
        |"FILLED"
      """.stripMargin
    decode[OrderState](json) should matchPattern {
      case Xor.Right(Filled) => //
    }
  }

  it can "parse a triggered order state from valid json" in {
    val json =
      """
        |"TRIGGERED"
      """.stripMargin
    decode[OrderState](json) should matchPattern {
      case Xor.Right(Triggered) => //
    }
  }

  it can "parse a cancelled order state from valid json" in {
    val json =
      """
        |"CANCELLED"
      """.stripMargin
    decode[OrderState](json) should matchPattern {
      case Xor.Right(Cancelled) => //
    }
  }

  it must "fail to parse an unknown order state from valid json" in {
    val json =
      """
        |"FOO"
      """.stripMargin
    decode[OrderState](json) should matchPattern {
      case Xor.Left(DecodingFailure("Unknown order state called 'FOO'", _)) => //
    }
  }

}
