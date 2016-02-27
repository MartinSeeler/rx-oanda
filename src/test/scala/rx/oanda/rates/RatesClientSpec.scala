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

package rx.oanda.rates

import akka.stream.testkit.scaladsl.TestSink
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment
import rx.oanda.rates.candles.{BidAskCandle, CandleTypes, MidpointCandle}

class RatesClientSpec extends FlatSpec with PropertyChecks with Matchers with FakeRateEndpoints {

  behavior of "The RatesClient"

  val authClient = new RatesClient(OandaEnvironment.TradePracticeEnvironment("token"))

  it must "retrieve all instruments with authentication" in {
    authClient.allInstruments(8954946L)
      .runWith(TestSink.probe[Instrument])
      .requestNext(Instrument("AUD_CAD", "AUD/CAD", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .requestNext(Instrument("AUD_CHF", "AUD/CHF", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .expectComplete()
  }

  it must "retrieve specific instruments with authentication" in {
    authClient.instruments(8954946L, "AUD_CAD" :: "AUD_CHF" :: Nil)
      .runWith(TestSink.probe[Instrument])
      .requestNext(Instrument("AUD_CAD", "AUD/CAD", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .requestNext(Instrument("AUD_CHF", "AUD/CHF", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .expectComplete()
  }

  it must "retrieve a specific instrument with authentication" in {
    authClient.instrument(8954946L, "AUD_CAD")
      .runWith(TestSink.probe[Instrument])
      .requestNext(Instrument("AUD_CAD", "AUD/CAD", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .expectComplete()
  }

  it must "retrieve prices for a specific instrument with authentication" in {
    authClient.price("EUR_USD")
      .runWith(TestSink.probe[Price])
      .requestNext(Price("EUR_USD", 1453847424597195L, 1.08646, 1.08668))
      .expectComplete()
  }

  it must "retrieve prices for specific instruments with authentication" in {
    authClient.prices("EUR_USD" :: "USD_CAD" :: Nil)
      .runWith(TestSink.probe[Price])
      .requestNext(Price("EUR_USD", 1453847424597195L, 1.08646, 1.08668))
      .requestNext(Price("USD_CAD", 1453847424597195L, 1.28646, 1.28668))
      .expectComplete()
  }

  it must "retrieve prices for specific instruments since a timestamp with authentication" in {
    authClient.pricesSince("EUR_USD" :: "USD_CAD" :: Nil, 1453847424597195L)
      .runWith(TestSink.probe[Price])
      .requestNext(Price("EUR_USD", 1453847424597195L, 1.08646, 1.08668))
      .requestNext(Price("USD_CAD", 1453847424597195L, 1.28646, 1.28668))
      .expectComplete()
  }

  it must "retrieve candles by count for a specific instrument in bidask format with authentication" in {
    authClient.candlesByCount("EUR_USD", 1)
      .runWith(TestSink.probe[BidAskCandle])
      .requestNext(BidAskCandle(1455488785000000L, 1.1001, 1.1005, 1.202, 1.204, 1.0001, 1.0005, 1.1101, 1.1105, 1337, true))
      .expectComplete()
  }

  it must "retrieve candles by date for a specific instrument in bidask format with authentication" in {
    authClient.candlesByDate("EUR_USD", 0, 100)
      .runWith(TestSink.probe[BidAskCandle])
      .requestNext(BidAskCandle(1455488788000000L, 1.2001, 1.2005, 1.302, 1.304, 1.1001, 1.1005, 1.2101, 1.2105, 42, true))
      .expectComplete()
  }

  it must "retrieve candles by count for a specific instrument in midpoint format with authentication" in {
    authClient.candlesByCount("EUR_USD", 1, candleType = CandleTypes.Midpoint)
      .runWith(TestSink.probe[MidpointCandle])
      .requestNext(MidpointCandle(1455488785000000L, 1.2, 1.4, 1.0, 1.1, 1337, true))
      .expectComplete()
  }

  it must "retrieve candles by date for a specific instrument in midpoint format with authentication" in {
    authClient.candlesByDate("EUR_USD", 0, 100, candleType = CandleTypes.Midpoint)
      .runWith(TestSink.probe[MidpointCandle])
      .requestNext(MidpointCandle(1455488788000000L, 1.3, 1.5, 1.1, 1.2, 42, true))
      .expectComplete()
  }

}
