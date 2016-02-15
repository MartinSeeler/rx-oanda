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
import io.circe.parser._
import org.scalatest._
import rx.oanda.utils.{Sell, Buy}

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

  it must "parse a MarketIfTouchedOrderCreated event from valid json" in {
    val json =
      """
        |{
        | "id": 176403882,
        | "accountId": 6765103,
        | "time": "1453326442000000",
        | "type": "MARKET_IF_TOUCHED_ORDER_CREATE",
        | "instrument": "EUR_USD",
        | "units": 2,
        | "side": "buy",
        | "price": 1,
        | "expiry": 1398902400,
        | "reason": "CLIENT_REQUEST"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(MarketIfTouchedOrderCreated(176403882L, 6765103L, 1453326442000000L, "EUR_USD", Buy, 2, 1, 1398902400L, "CLIENT_REQUEST", None, None, None, None, None)) ⇒
    }
  }

  it must "parse a OrderUpdated event from valid json" in {
    val json =
      """
        |{
        | "id" : 176403883,
        | "accountId" : 6765103,
        | "time" : "1453326442000000",
        | "type" : "ORDER_UPDATE",
        | "instrument" : "EUR_USD",
        | "units" : 3,
        | "price" : 1,
        | "expiry" : 1398902400,
        | "orderId" : 176403880,
        | "reason" : "REPLACES_ORDER"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(OrderUpdated(176403883L, 6765103L, 1453326442000000L, "EUR_USD", 3, 1, 176403880L, "REPLACES_ORDER", None, None, None, None, None)) ⇒
    }
  }

  it must "parse a OrderCanceled event from valid json" in {
    val json =
      """
        |{
        | "id" : 176403881,
        | "accountId" : 6765103,
        | "time" : "1453326442000000",
        | "type" : "ORDER_CANCEL",
        | "orderId" : 176403880,
        | "reason" : "CLIENT_REQUEST"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(OrderCanceled(176403881L, 6765103L, 1453326442000000L, 176403880L, "CLIENT_REQUEST")) ⇒
    }
  }

  it must "parse a OrderFilled event from valid json" in {
    val json =
      """
        |{
        | "id" : 175685908,
        | "accountId" : 2610411,
        | "time" : "1453326442000000",
        | "type" : "ORDER_FILLED",
        | "instrument" : "EUR_USD",
        | "units" : 2,
        | "side" : "buy",
        | "price" : 1.3821,
        | "pl" : 0,
        | "interest" : 0,
        | "accountBalance" : 100000,
        | "orderId" : 175685907,
        | "tradeOpened" : {
        |     "id" : 175685908,
        |     "units" : 2
        | }
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(OrderFilled(175685908L, 2610411L, 1453326442000000L, "EUR_USD", 2, Buy, 1.3821, 0, 0, 100000, 175685907L, None, None, None, None, None, Some(TradeOpened(175685908L, 2)), None)) ⇒
    }
  }

  it must "parse a TradeUpdated event from valid json" in {
    val json =
      """
        |{
        | "id": 176403884,
        | "accountId": 6765103,
        | "time": "1453326442000000",
        | "type": "TRADE_UPDATE",
        | "instrument": "EUR_USD",
        | "units": 2,
        | "stopLossPrice": 1.1,
        | "tradeId": 176403879
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(TradeUpdated(176403884L, 6765103L, 1453326442000000L, "EUR_USD", 2, 176403879L, None, Some(1.1), None)) ⇒
    }
  }

  it must "parse a TradeClosed event from valid json" in {
    val json =
      """
        |{
        | "id" : 176403885,
        | "accountId" : 6765103,
        | "time" : "1453326442000000",
        | "type" : "TRADE_CLOSE",
        | "instrument" : "EUR_USD",
        | "units" : 2,
        | "side" : "sell",
        | "price" : 1.25918,
        | "pl" : 0.0119,
        | "interest" : 0,
        | "accountBalance" : 100000.0119,
        | "tradeId" : 176403879
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(TradeClosed(176403885L, 6765103L, 1453326442000000L, "EUR_USD", 2, Sell, 1.25918, 0.0119, 0, 100000.0119, 176403879L)) ⇒
    }
  }

  it must "parse a MigrateTradeClosed event from valid json" in {
    val json =
      """
        |{
        | "id" : 176403885,
        | "accountId" : 6765103,
        | "time" : "1453326442000000",
        | "type" : "MIGRATE_TRADE_CLOSE",
        | "instrument" : "EUR_USD",
        | "units" : 2,
        | "side" : "sell",
        | "price" : 1.25918,
        | "pl" : 0.0119,
        | "interest" : 0,
        | "accountBalance" : 100000.0119,
        | "tradeId" : 176403879
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(MigrateTradeClosed(176403885L, 6765103L, 1453326442000000L, "EUR_USD", 2, Sell, 1.25918, 0.0119, 0, 100000.0119, 176403879L)) ⇒
    }
  }

  it must "parse a MigrateTradeOpened event from valid json" in {
    val json =
      """
        |{
        | "id" : 175685908,
        | "accountId" : 2610411,
        | "time" : "1453326442000000",
        | "type" : "MIGRATE_TRADE_OPEN",
        | "instrument" : "EUR_USD",
        | "units" : 2,
        | "side" : "buy",
        | "price" : 1.3821,
        | "tradeOpened" : {
        |     "id" : 175685908,
        |     "units" : 2
        | }
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(MigrateTradeOpened(175685908L, 2610411L, 1453326442000000L, "EUR_USD", 2, Buy, 1.3821, None, None, None, TradeOpened(175685908L, 2))) ⇒
    }
  }

  it must "parse a TakeProfitFilled event from valid json" in {
    val json =
      """
        |{
        | "id": 175685954,
        | "accountId": 1491998,
        | "time": "1453326442000000",
        | "type": "TAKE_PROFIT_FILLED",
        | "units": 10,
        | "tradeId": 175685930,
        | "instrument": "EUR_USD",
        | "side": "sell",
        | "price": 1.38231,
        | "pl": 0.0001,
        | "interest": 0,
        | "accountBalance": 100000.0001
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(TakeProfitFilled(175685954L, 1491998L, 1453326442000000L, "EUR_USD", 10, Sell, 1.38231, 0.0001, 0, 100000.0001, 175685930L)) ⇒
    }
  }

  it must "parse a StopLossFilled event from valid json" in {
    val json =
      """
        |{
        | "id": 175685918,
        | "accountId": 1403479,
        | "time": "1453326442000000",
        | "type": "STOP_LOSS_FILLED",
        | "units": 10,
        | "tradeId": 175685917,
        | "instrument": "EUR_USD",
        | "side": "sell",
        | "price": 1.3821,
        | "pl": -0.0003,
        | "interest": 0,
        | "accountBalance": 99999.9997
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(StopLossFilled(175685918L, 1403479L, 1453326442000000L, "EUR_USD", 10, Sell, 1.3821, -0.0003, 0, 99999.9997, 175685917L)) ⇒
    }
  }

  it must "parse a TrailingStopFilled event from valid json" in {
    val json =
      """
        |{
        | "id" : 175739353,
        | "accountId" : 1491998,
        | "time" : "1453326442000000",
        | "type" : "TRAILING_STOP_FILLED",
        | "units" : 10,
        | "tradeId" : 175739352,
        | "instrument" : "EUR_USD",
        | "side" : "sell",
        | "price" : 1.38137,
        | "pl" : -0.0009,
        | "interest" : 0,
        | "accountBalance" : 99999.9992
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(TrailingStopFilled(175739353L, 1491998L, 1453326442000000L, "EUR_USD", 10, Sell, 1.38137, -0.0009, 0, 99999.9992, 175739352L)) ⇒
    }
  }

  it must "parse a MarginCallEntered event from valid json" in {
    val json =
      """
        |{
        | "id" : 175739360,
        | "accountId" : 1491998,
        | "time" : "1453326442000000",
        | "type" : "MARGIN_CALL_ENTER"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(MarginCallEntered(175739360L, 1491998L, 1453326442000000L)) ⇒
    }
  }

  it must "parse a MarginCallExited event from valid json" in {
    val json =
      """
        |{
        | "id" : 175739360,
        | "accountId" : 1491998,
        | "time" : "1453326442000000",
        | "type" : "MARGIN_CALL_EXIT"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(MarginCallExited(175739360L, 1491998L, 1453326442000000L)) ⇒
    }
  }

  it must "parse a MarginCloseoutTriggered event from valid json" in {
    val json =
      """
        |{
        | "id" : 176403889,
        | "accountId" : 6765103,
        | "time" : "1453326442000000",
        | "type" : "MARGIN_CLOSEOUT",
        | "instrument" : "EUR_USD",
        | "units" : 2,
        | "side" : "sell",
        | "price" : 1.25918,
        | "pl" : 0.0119,
        | "interest" : 0,
        | "accountBalance" : 100000.0119,
        | "tradeId" : 176403879
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(MarginCloseoutTriggered(176403889L, 6765103L, 1453326442000000L, "EUR_USD", 2, Sell, 1.25918, 0.0119, 0, 100000.0119, 176403879L)) ⇒
    }
  }

  it must "parse a MarginRateChanged event from valid json" in {
    val json =
      """
        |{
        | "id" : 175739360,
        | "accountId" : 1491998,
        | "time" : "1453326442000000",
        | "type" : "SET_MARGIN_RATE",
        | "rate" : 0.02
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(MarginRateChanged(175739360L, 1491998L, 1453326442000000L, 0.02)) ⇒
    }
  }

  it must "parse a FundsTransfered event from valid json" in {
    val json =
      """
        |{
        | "id" : 176403878,
        | "accountId" : 6765103,
        | "time" : "1453326442000000",
        | "type" : "TRANSFER_FUNDS",
        | "amount" : 100000,
        | "accountBalance" : 100000,
        | "reason" : "CLIENT_REQUEST"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(FundsTransfered(176403878L, 6765103L, 1453326442000000L, 100000.0, 100000.0, "CLIENT_REQUEST")) ⇒
    }
  }

  it must "parse a DailyInterest event from valid json" in {
    val json =
      """
        |{
        | "id" : 175739363,
        | "accountId" : 1491998,
        | "time" : "1453326442000000",
        | "type" : "DAILY_INTEREST",
        | "instrument" : "EUR_USD",
        | "interest" : 10.0414,
        | "accountBalance" : 99999.9992
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(DailyInterest(175739363L, 1491998L, 1453326442000000L, "EUR_USD", 10.0414, 99999.9992)) ⇒
    }
  }

  it must "parse a Fee event from valid json" in {
    val json =
      """
        |{
        | "id" : 175739369,
        | "accountId" : 1491998,
        | "time" : "1453326442000000",
        | "type" : "FEE",
        | "amount" : -10.0414,
        | "accountBalance" : 99999.9992,
        | "reason" : "FUNDS"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Right(Fee(175739369L, 1491998L, 1453326442000000L, -10.0414, 99999.9992, "FUNDS")) ⇒
    }
  }



  it must "fail to parse an event without a type field" in {
    val json =
      """
        |{
        | "id" : 175739369,
        | "accountId" : 1491998,
        | "time" : "1453326442000000",
        | "amount" : -10.0414,
        | "accountBalance" : 99999.9992,
        | "reason" : "FUNDS"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

  it must "fail to parse an event with an unknown type field" in {
    val json =
      """
        |{
        | "id" : 175739369,
        | "accountId" : 1491998,
        | "time" : "1453326442000000",
        | "type" : "FOOBAR",
        | "amount" : -10.0414,
        | "accountBalance" : 99999.9992,
        | "reason" : "FUNDS"
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

  it must "fail to parse something else" in {
    val json =
      """
        |{
        | "instrument": "EUR_USD",
        | "time": "1453847424597195",
        | "bid": 1.08646,
        | "ask": 1.08668
        |}
      """.stripMargin
    decode[OandaEvent](json) should matchPattern {
      case Xor.Left(e: DecodingFailure) ⇒ //...
    }
  }

}
