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

package rx.oanda

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.testkit.TestFrameworkInterface.Scalatest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.scalatest._
import rx.oanda.OandaEnvironment._

class ConnectionFlowSpec extends FlatSpec with Matchers with Scalatest {

  behavior of "The ConnectionFlow"

  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()
  import sys.dispatcher

  it must "provide the correct api connection for the sandbox environment" in {
    val streamFlow = OandaEnvironment.SandboxEnvironment.apiFlow[Long]
    val pool = Source.empty.viaMat(streamFlow)(Keep.right).toMat(Sink.ignore)(Keep.left).run()
    pool.setup.host should be ("api-sandbox.oanda.com")
    pool.setup.setup.httpsContext shouldBe empty
  }

  it must "provide the correct stream connection for the sandbox environment" in {
    val apiFlow = OandaEnvironment.SandboxEnvironment.streamFlow[Long]
    val pool = Source.empty.viaMat(apiFlow)(Keep.right).toMat(Sink.ignore)(Keep.left).run()
    pool.setup.host should be ("stream-sandbox.oanda.com")
    pool.setup.setup.httpsContext shouldBe empty
  }

  it must "provide the correct api connection for the trade practice environment" in {
    val apiFlow = OandaEnvironment.TradePracticeEnvironment("token").apiFlow[Long]
    val pool = Source.empty.viaMat(apiFlow)(Keep.right).toMat(Sink.ignore)(Keep.left).run()
    pool.setup.host should be ("api-fxpractice.oanda.com")
    pool.setup.setup.httpsContext shouldBe defined
  }

  it must "provide the correct stream connection for the trade practice environment" in {
    val streamFlow = OandaEnvironment.TradePracticeEnvironment("token").streamFlow[Long]
    val pool = Source.empty.viaMat(streamFlow)(Keep.right).toMat(Sink.ignore)(Keep.left).run()
    pool.setup.host should be ("stream-fxpractice.oanda.com")
    pool.setup.setup.httpsContext shouldBe defined
  }

  it must "provide the correct api connection for the trade environment" in {
    val apiFlow = OandaEnvironment.TradeEnvironment("token").apiFlow[Long]
    val pool = Source.empty.viaMat(apiFlow)(Keep.right).toMat(Sink.ignore)(Keep.left).run()
    pool.setup.host should be ("api-fxtrade.oanda.com")
    pool.setup.setup.httpsContext shouldBe defined
  }

  it must "provide the correct stream connection for the trade environment" in {
    val streamFlow = OandaEnvironment.TradeEnvironment("token").streamFlow[Long]
    val pool = Source.empty.viaMat(streamFlow)(Keep.right).toMat(Sink.ignore)(Keep.left).run()
    pool.setup.host should be ("stream-fxtrade.oanda.com")
    pool.setup.setup.httpsContext shouldBe defined
  }

  def cleanUp(): Unit = Http().shutdownAllConnectionPools().onComplete(_ ⇒ sys.shutdown())

}
