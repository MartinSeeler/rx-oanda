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
import io.circe.parser._
import org.scalatest._

class OandaErrorDecoderSpec extends FlatSpec with Matchers {

  behavior of "The Oanda Error Decoder"

  it must "parse an invalid argument error" in {
    val json =
      """
        |{
        | "code" : 1,
        | "message" : "Invalid or malformed argument: accountId",
        | "moreInfo" : "http:\/\/developer.oanda.com\/docs\/v1\/troubleshooting\/#errors"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(InvalidArgument("Invalid or malformed argument: accountId")) ⇒
    }
  }

  it must "parse a missing argument error" in {
    val json =
      """
        |{
        | "code": 2,
        | "message": "Missing required argument: instruments",
        | "moreInfo": "http://developer.oanda.com/docs/v1/troubleshooting/#errors"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(MissingArgument("Missing required argument: instruments")) ⇒
    }
  }

  it must "parse a missing authorization error" in {
    val json =
      """
        |{
        | "code" : 3,
        | "message" : "This request requires authorization",
        | "moreInfo" : "http:\/\/developer.oanda.com\/docs\/v1\/auth\/#overview"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(MissingAuthorization("This request requires authorization")) ⇒
    }
  }

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

  it must "parse an invalid range error" in {
    val json =
      """
        |{
        | "code": 36,
        | "message": "The value specified is not in the valid range: Resulting candle count is larger than maximum allowed: 5000",
        | "moreInfo": "http://developer.oanda.com/docs/v1/troubleshooting/#errors"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(InvalidRange("The value specified is not in the valid range: Resulting candle count is larger than maximum allowed: 5000")) ⇒
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

  it must "parse an invalid timestamp error" in {
    val json =
      """
        |{
        | "code" : 45,
        | "message" : "Invalid timestamp: [since] parameter",
        | "moreInfo" : "http:\/\/developer.oanda.com\/docs\/v1\/troubleshooting\/#errors"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(InvalidTimestamp("Invalid timestamp: [since] parameter")) ⇒
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

  it must "parse an argument conflict error" in {
    val json =
      """
        |{
        | "code": 47,
        | "message": "Argument conflict found: [includeFirst] cannot be specified if [start] is not specified.",
        | "moreInfo": "http://developer.oanda.com/docs/v1/troubleshooting/#errors"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(ArgumentConflict("Argument conflict found: [includeFirst] cannot be specified if [start] is not specified.")) ⇒
    }
  }

  it must "parse a rate limit violation error" in {
    val json =
      """
        |{
        | "code": 68,
        | "message": "Rate limit violation of newly established connections. Allowed rate: 2 connections per second",
        | "moreInfo": "http://developer.oanda.com/docs/v1/troubleshooting/#errors"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(RateLimitViolation("Rate limit violation of newly established connections. Allowed rate: 2 connections per second")) ⇒
    }
  }


  it must "parse an unknown oanda error if an error code is provided but not mapped yet" in {
    val json =
      """
        |{
        | "code": 1337,
        | "message": "There was an error which is not mapped to any case class yet!",
        | "moreInfo": "http://developer.oanda.com/docs/v1/troubleshooting/#errors"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Right(UnknownOandaError(1337, "There was an error which is not mapped to any case class yet!", "http://developer.oanda.com/docs/v1/troubleshooting/#errors")) ⇒
    }
  }

  it must "fail to parse an error without an error code" in {
    val json =
      """
        |{
        | "message": "This should fail",
        | "moreInfo": "htt://example.com"
        |}
      """.stripMargin
    decode[OandaError](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒
    }
  }

}
