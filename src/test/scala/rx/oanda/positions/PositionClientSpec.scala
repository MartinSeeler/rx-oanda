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

package rx.oanda.positions

import akka.stream.testkit.scaladsl.TestSink
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment
import rx.oanda.utils.{Buy, Sell}

import scala.collection.generic.SeqFactory

class PositionClientSpec extends FlatSpec with PropertyChecks with Matchers with FakePositionEndpoint {

  behavior of "The PositionClient"

  val testClient = new PositionClient(OandaEnvironment.TradeEnvironment("token"))

  it must "stream the position by an instrument" in {
    testClient.positionByInstrument(6765103L, "EUR_USD")
      .runWith(TestSink.probe[Position])
      .requestNext(Position(Sell, "EUR_USD", 9, 1.3093))
      .expectComplete()
  }

  it must "stream all open positions for an account" in {
    testClient.positions(6765103L)
      .runWith(TestSink.probe[Position])
      .requestNext(Position(Buy, "EUR_USD", 4741, 1.3626))
      .requestNext(Position(Sell, "USD_CAD", 30, 1.11563))
      .requestNext(Position(Buy, "USD_JPY", 88, 102.455))
      .expectComplete()
  }

  it must "close an open position for an instrument" in {
    testClient.closePosition(6765103L, "EUR_USD")
      .runWith(TestSink.probe[ClosedPosition])
      .requestNext(ClosedPosition(Vector(12345, 12346, 12347), "EUR_USD", 1234, 1.2345))
      .expectComplete()
  }

}
