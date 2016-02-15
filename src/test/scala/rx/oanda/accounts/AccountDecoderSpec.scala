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

class AccountDecoderSpec extends FlatSpec with Matchers {

  behavior of "The Account Decoder"

  it must "parse an account from valid json" in {
    val json =
      """
        |{
        | "accountId": 8954947,
        | "accountName": "Primary",
        | "balance": 100000,
        | "unrealizedPl": 1.1,
        | "realizedPl": -2.2,
        | "marginUsed": 3.3,
        | "marginAvail": 100000,
        | "openTrades": 1,
        | "openOrders": 2,
        | "marginRate": 0.05,
        | "accountCurrency": "USD"
        |}
      """.stripMargin
    decode[Account](json) should matchPattern {
      case Xor.Right(Account(8954947L, "Primary", 100000, 1.1, -2.2, 3.3, 100000, 1, 2, 0.05, "USD")) ⇒
    }
  }

  it must "fail when a property is missing" in {
    val json =
      """
        |{
        | "accountId": 8954947,
        | "accountName": "Primary",
        | "unrealizedPl": 1.1,
        | "realizedPl": -2.2,
        | "marginUsed": 3.3,
        | "marginAvail": 100000,
        | "openTrades": 1,
        | "openOrders": 2,
        | "marginRate": 0.05,
        | "accountCurrency": "USD"
        |}
      """.stripMargin
    decode[Account](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

}
