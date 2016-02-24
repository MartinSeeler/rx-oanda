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
import cats.data.Xor
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment
import rx.oanda.rates.candles.{BidAskCandle, CandleTypes, MidpointCandle}
import rx.oanda.utils.Heartbeat

class RatesClientStreamingSpec extends FlatSpec with PropertyChecks with Matchers with FakeRateStreamingEndpoints {

  behavior of "The RatesClient"

  val noAuthClient = new RatesClient(OandaEnvironment.SandboxEnvironment)
  val authClient = new RatesClient(OandaEnvironment.TradePracticeEnvironment("token"))

  it must "stream prices and heartbeats with authentication" in {
    authClient.livePrices(8954946L, "AUD_CAD" :: "AUD_CHF" :: Nil)
      .runWith(TestSink.probe[Xor[Price, Heartbeat]])
      .requestNext(Xor.left(Price("AUD_CAD", 1391114828000000L, 0.98114, 0.98139)))
      .requestNext(Xor.left(Price("AUD_CHF", 1391114828000000L, 0.79353, 0.79382)))
      .requestNext(Xor.left(Price("AUD_CHF", 1391114831000000L, 0.79355, 0.79387)))
      .requestNext(Xor.right(Heartbeat(1391114831000000L)))
      .requestNext(Xor.left(Price("AUD_CHF", 1391114831000000L, 0.79357, 0.79390)))
      .requestNext(Xor.left(Price("AUD_CAD", 1391114834000000L, 0.98112, 0.98138)))
      .expectComplete()
  }

}
