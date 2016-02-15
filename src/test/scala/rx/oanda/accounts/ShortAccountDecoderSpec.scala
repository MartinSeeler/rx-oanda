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

package rx.oanda.accounts

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest._

class ShortAccountDecoderSpec extends FlatSpec with Matchers {

  behavior of "The ShortAccount Decoder"

  it must "parse a short account from valid json" in {
    val json =
      """
        |{
        | "accountId" : 8954947,
        | "accountName" : "Primary",
        | "accountCurrency" : "USD",
        | "marginRate" : 0.05
        |}
      """.stripMargin
    decode[ShortAccount](json) should matchPattern {
      case Xor.Right(ShortAccount(8954947L, "Primary", "USD", 0.05)) ⇒
    }
  }

  it must "fail when a property is missing" in {
    val json =
      """
        |{
        | "accountId" : 8954947,
        | "accountCurrency" : "USD",
        | "marginRate" : 0.05
        |}
      """.stripMargin
    decode[ShortAccount](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

  it must "parse multiple short accounts from json array" in {
    val json =
      """
        |{
        | "accounts": [
        |   {
        |    "accountId" : 8954947,
        |    "accountName" : "Primary",
        |    "accountCurrency" : "USD",
        |    "marginRate" : 0.05
        |   },
        |   {
        |    "accountId" : 8954950,
        |    "accountName" : "SweetHome",
        |    "accountCurrency" : "CAD",
        |    "marginRate" : 0.02
        |   }
        | ]
        |}
      """.stripMargin
    decode[Vector[ShortAccount]](json) should matchPattern {
      case Xor.Right(Vector(ShortAccount(8954947L, "Primary", "USD", 0.05), ShortAccount(8954950L, "SweetHome", "CAD", 0.02))) ⇒ //...
    }
  }

}
