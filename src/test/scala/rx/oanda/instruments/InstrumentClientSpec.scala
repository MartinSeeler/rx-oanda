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

package rx.oanda.instruments

import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment

class InstrumentClientSpec extends FlatSpec with PropertyChecks with Matchers with FakeInstrumentEndpoints {

  behavior of "The InstrumentClient"

  val client = new InstrumentClient(OandaEnvironment.TradePracticeEnvironment("token"))

  it must "retrieve all instruments with authentication" in {
    client.allInstruments(8954946L)
      .runWith(TestSink.probe[Instrument])
      .requestNext(Instrument("AUD_CAD", "AUD/CAD", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .requestNext(Instrument("AUD_CHF", "AUD/CHF", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .expectComplete()
  }

  it must "retrieve specific instruments with authentication" in {
    client.instruments(8954946L, "AUD_CAD" :: "AUD_CHF" :: Nil)
      .runWith(TestSink.probe[Instrument])
      .requestNext(Instrument("AUD_CAD", "AUD/CAD", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .requestNext(Instrument("AUD_CHF", "AUD/CHF", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .expectComplete()
  }

  it must "retrieve a specific instrument with authentication" in {
    client.instrument(8954946L, "AUD_CAD")
      .runWith(TestSink.probe[Instrument])
      .requestNext(Instrument("AUD_CAD", "AUD/CAD", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, halted = true))
      .expectComplete()
  }

}
