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

package rx.oanda.account

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest.{FlatSpec, Matchers}

class AccountFinancingModeDecoderSpec extends FlatSpec with Matchers {

  behavior of "The AccountFinancingMode Decoder"

  it can "parse NoFinancing AccountFinancingMode from valid json" in {
    val json =
      """
        |"NO_FINANCING"
      """.stripMargin
    decode[AccountFinancingMode](json) should matchPattern {
      case Xor.Right(NoFinancing) => //
    }
  }

  it can "parse SecondBySecond AccountFinancingMode from valid json" in {
    val json =
      """
        |"SECOND_BY_SECOND"
      """.stripMargin
    decode[AccountFinancingMode](json) should matchPattern {
      case Xor.Right(SecondBySecond) => //
    }
  }

  it can "parse Daily AccountFinancingMode from valid json" in {
    val json =
      """
        |"DAILY"
      """.stripMargin
    decode[AccountFinancingMode](json) should matchPattern {
      case Xor.Right(Daily) => //
    }
  }

  it must "fail to parse an unknown AccountFinancingMode" in {
    val json =
      """
        |"FOOBAR"
      """.stripMargin
    decode[AccountFinancingMode](json) should matchPattern {
      case Xor.Left(DecodingFailure(_, _)) => //
    }
  }

}
