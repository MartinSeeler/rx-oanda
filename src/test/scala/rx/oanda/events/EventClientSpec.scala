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

import akka.stream.testkit.scaladsl.TestSink
import cats.data.Xor
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment
import rx.oanda.utils.{Sell, Buy, Heartbeat}

class EventClientSpec extends FlatSpec with PropertyChecks with Matchers with FakeEventEndpoints  {

  behavior of "The EventClient"

  val testClient = new EventClient(OandaEnvironment.TradePracticeEnvironment("token"))

  it must "parse events and heartbeats from the streaming endpoint" in {
    testClient.liveEvents()
      .runWith(TestSink.probe[Xor[OandaEvent, Heartbeat]])
      .requestNext(Xor.Left(MarketOrderCreated(176403879L, 6765103L, 1453326442000000L, "EUR_USD", Buy, 2, 1.25325, 0, 0, 100000, None, None, None, None, None, Some(TradeOpened(176403879L, 2)), None)))
      .requestNext(Xor.Left(StopOrderCreated(176403886L, 6765103L, 1453326442000000L, "EUR_USD", Buy, 2, 1, 1398902400L, "CLIENT_REQUEST", None, None, None, None, None)))
      .requestNext(Xor.Left(LimitOrderCreated(176403886L, 6765103L, 1453326442000000L, "EUR_USD", Buy, 2, 1, 1398902400L, "CLIENT_REQUEST", None, None, None, None, None)))
      .requestNext(Xor.Left(MarketIfTouchedOrderCreated(176403882L, 6765103L, 1453326442000000L, "EUR_USD", Buy, 2, 1, 1398902400L, "CLIENT_REQUEST", None, None, None, None, None)))
      .requestNext(Xor.Left(OrderUpdated(176403883L, 6765103L, 1453326442000000L, "EUR_USD", 3, 1, 176403880L, "REPLACES_ORDER", None, None, None, None, None)))
      .requestNext(Xor.Left(OrderCanceled(176403881L, 6765103L, 1453326442000000L, 176403880L, "CLIENT_REQUEST")))
      .requestNext(Xor.Left(OrderFilled(175685908L, 2610411L, 1453326442000000L, "EUR_USD", 2, Buy, 1.3821, 0, 0, 100000, 175685907L, None, None, None, None, None, Some(TradeOpened(175685908L, 2)), None)))
      .requestNext(Xor.Left(TradeUpdated(176403884L, 6765103L, 1453326442000000L, "EUR_USD", 2, 176403879L, None, Some(1.1), None)))
      .requestNext(Xor.Left(TradeClosed(176403885L, 6765103L, 1453326442000000L, "EUR_USD", 2, Sell, 1.25918, 0.0119, 0, 100000.0119, 176403879L)))
      .requestNext(Xor.Right(Heartbeat(1391114831000000L)))
      .requestNext(Xor.Left(MigrateTradeClosed(176403885L, 6765103L, 1453326442000000L, "EUR_USD", 2, Sell, 1.25918, 0.0119, 0, 100000.0119, 176403879L)))
      .requestNext(Xor.Left(MigrateTradeOpened(175685908L, 2610411L, 1453326442000000L, "EUR_USD", 2, Buy, 1.3821, None, None, None, TradeOpened(175685908L, 2))))
      .requestNext(Xor.Left(TakeProfitFilled(175685954L, 1491998L, 1453326442000000L, "EUR_USD", 10, Sell, 1.38231, 0.0001, 0, 100000.0001, 175685930L)))
      .requestNext(Xor.Left(StopLossFilled(175685918L, 1403479L, 1453326442000000L, "EUR_USD", 10, Sell, 1.3821, -0.0003, 0, 99999.9997, 175685917L)))
      .requestNext(Xor.Left(TrailingStopFilled(175739353L, 1491998L, 1453326442000000L, "EUR_USD", 10, Sell, 1.38137, -0.0009, 0, 99999.9992, 175739352L)))
      .requestNext(Xor.Left(MarginCallEntered(175739360L, 1491998L, 1453326442000000L)))
      .requestNext(Xor.Left(MarginCallExited(175739360L, 1491998L, 1453326442000000L)))
      .requestNext(Xor.Left(MarginCloseoutTriggered(176403889L, 6765103L, 1453326442000000L, "EUR_USD", 2, Sell, 1.25918, 0.0119, 0, 100000.0119, 176403879L)))
      .requestNext(Xor.Left(MarginRateChanged(175739360L, 1491998L, 1453326442000000L, 0.02)))
      .requestNext(Xor.Left(FundsTransfered(176403878L, 6765103L, 1453326442000000L, 100000.0, 100000.0, "CLIENT_REQUEST")))
      .requestNext(Xor.Left(DailyInterest(175739363L, 1491998L, 1453326442000000L, "EUR_USD", 10.0414, 99999.9992)))
      .requestNext(Xor.Left(Fee(175739369L, 1491998L, 1453326442000000L, -10.0414, 99999.9992, "FUNDS")))
      .expectComplete()
  }

}
