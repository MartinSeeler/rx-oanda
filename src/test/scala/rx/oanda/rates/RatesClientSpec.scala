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

class RatesClientSpec extends FlatSpec with PropertyChecks with Matchers with FakeRateEndpoints {

  behavior of "The RatesClient"

  val noAuthClient = new RatesClient(OandaEnvironment.SandboxEnvironment)
  val authClient = new RatesClient(OandaEnvironment.TradePracticeEnvironment("token"))

  it must "retrieve all instruments with authentication" in {
    authClient.allInstruments(8954946L)
      .runWith(TestSink.probe[Instrument])
      .requestNext(Instrument("AUD_CAD", "AUD/CAD", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .requestNext(Instrument("AUD_CHF", "AUD/CHF", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .expectComplete()
  }

  it must "retrieve all instruments without authentication" in {
    noAuthClient.allInstruments(8954946L)
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

  it must "retrieve specific instruments without authentication" in {
    noAuthClient.instruments(8954946L, "AUD_CAD" :: "AUD_CHF" :: Nil)
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

  it must "retrieve a specific instrument without authentication" in {
    noAuthClient.instrument(8954946L, "AUD_CAD")
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

  it must "retrieve prices for a specific instrument without authentication" in {
    noAuthClient.price("EUR_USD")
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

  it must "retrieve prices for specific instruments without authentication" in {
    noAuthClient.prices("EUR_USD" :: "USD_CAD" :: Nil)
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

  it must "retrieve prices for specific instruments since a timestamp without authentication" in {
    noAuthClient.pricesSince("EUR_USD" :: "USD_CAD" :: Nil, 1453847424597195L)
      .runWith(TestSink.probe[Price])
      .requestNext(Price("EUR_USD", 1453847424597195L, 1.08646, 1.08668))
      .requestNext(Price("USD_CAD", 1453847424597195L, 1.28646, 1.28668))
      .expectComplete()
  }

}
