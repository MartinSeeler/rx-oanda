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
import io.circe.parse._
import org.scalatest._

class TestAccountDecoderSpec extends FlatSpec with Matchers {

  behavior of "The TestAccount Decoder"

  it must "parse a test account from valid json" in {
    val json =
      """
        |{
        | "username" : "keith",
        | "password" : "Rocir~olf4",
        | "accountId" : 8954947
        |}
      """.stripMargin
    decode[TestAccount](json) should matchPattern {
      case Xor.Right(TestAccount("keith", "Rocir~olf4", 8954947L)) ⇒
    }
  }

  it must "fail on missing property" in {
    val json =
      """
        {
        | "username" : "keith",
        | "accountId" : 8954947
        |}
      """.stripMargin
    decode[BaseAccount](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

}
