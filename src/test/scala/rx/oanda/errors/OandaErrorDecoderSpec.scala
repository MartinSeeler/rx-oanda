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

package rx.oanda.errors

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parse._
import org.scalatest._

class OandaErrorDecoderSpec extends FlatSpec with Matchers {

  behavior of "The Oanda Error Decoder"

  it must "parse an invalid authorization error" in {
    val json =
      """
        |{
        | "code" : 4,
        | "message" : "The access token provided does not allow this request to be made",
        | "moreInfo" : "http:\/\/developer.oanda.com\/docs\/v1\/auth\/#overview"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(InvalidAuhtorization("The access token provided does not allow this request to be made")) ⇒
    }
  }

  it must "parse a malformed query string error" in {
    val json =
      """
        |{
        | "code" : 40,
        | "message" : "Received request with malformed query string: 'instruments=EUR_USD,EUR_GBP'",
        | "moreInfo" : "http:\/\/developer.oanda.com\/docs\/v1\/troubleshooting\/#errors"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(MalformedQueryString("Received request with malformed query string: 'instruments=EUR_USD,EUR_GBP'")) ⇒
    }
  }

  it must "parse an invalid instrument error" in {
    val json =
      """
        |{
        | "code" : 46,
        | "message" : "Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument",
        | "moreInfo" : "http:\/\/developer.oanda.com\/docs\/v1\/troubleshooting\/#errors"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(InvalidInstrument("Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument")) ⇒
    }
  }

}
