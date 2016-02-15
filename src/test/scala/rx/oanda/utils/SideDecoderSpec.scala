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

package rx.oanda.utils

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest._

class SideDecoderSpec extends FlatSpec with Matchers {

  behavior of "The Side Decoder"

  it must "parse a Buy instance on valid json" in {
    val json = "\"buy\""
    decode[Side](json) should matchPattern {
      case Xor.Right(Buy) ⇒
    }
  }

  it must "parse a Sell instance on valid json" in {
    val json = "\"sell\""
    decode[Side](json) should matchPattern {
      case Xor.Right(Sell) ⇒
    }
  }

  it must "fail on invalid side" in {
    val json = "\"flat\""
    decode[Side](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

}
