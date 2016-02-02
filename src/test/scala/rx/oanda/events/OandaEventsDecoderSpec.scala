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

package rx.oanda.events

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parse._
import org.scalatest._
import rx.oanda.utils.Buy

class OandaEventsDecoderSpec extends FlatSpec with Matchers {

  behavior of "The OandaEvents Decoder"

  it must "parse a MarketOrderCreated event from valid json" in {
    val json =
      """
        |{
        | "id": 176403879,
        | "accountId": 6765103,
        | "time": "1453326442000000",
        | "type": "MARKET_ORDER_CREATE",
        | "instrument": "EUR_USD",
        | "units": 2,
        | "side": "buy",
        | "price": 1.25325,
        | "pl": 0,
        | "interest": 0,
        | "accountBalance": 100000,
        | "tradeOpened": {
        |  "id": 176403879,
        |  "units": 2
        | }
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(MarketOrderCreated(176403879L, 6765103L, 1453326442000000L, "EUR_USD", Buy, 2, 1.25325, 0, 0, 100000, None, None, None, None, None, Some(TradeOpened(176403879L, 2)), None)) ⇒
    }
  }

  it must "parse a StopOrderCreated event from valid json" in {
    val json =
      """
        |{
        | "id" : 176403886,
        | "accountId" : 6765103,
        | "time" : "1453326442000000",
        | "type" : "STOP_ORDER_CREATE",
        | "instrument" : "EUR_USD",
        | "units" : 2,
        | "side" : "buy",
        | "price" : 1,
        | "expiry" : 1398902400,
        | "reason" : "CLIENT_REQUEST"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(StopOrderCreated(176403886L, 6765103L, 1453326442000000L, "EUR_USD", Buy, 2, 1, 1398902400L, "CLIENT_REQUEST", None, None, None, None, None)) ⇒
    }
  }

  it must "parse a LimitOrderCreated event from valid json" in {
    val json =
      """
        |{
        | "id" : 176403886,
        | "accountId" : 6765103,
        | "time" : "1453326442000000",
        | "type" : "LIMIT_ORDER_CREATE",
        | "instrument" : "EUR_USD",
        | "units" : 2,
        | "side" : "buy",
        | "price" : 1,
        | "expiry" : 1398902400,
        | "reason" : "CLIENT_REQUEST"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(LimitOrderCreated(176403886L, 6765103L, 1453326442000000L, "EUR_USD", Buy, 2, 1, 1398902400L, "CLIENT_REQUEST", None, None, None, None, None)) ⇒
    }
  }

}
