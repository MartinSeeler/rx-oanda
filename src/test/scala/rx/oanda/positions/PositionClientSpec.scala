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

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.testkit.TestFrameworkInterface.Scalatest
import akka.stream.ActorMaterializer
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment

class PositionClientSpec extends FlatSpec with PropertyChecks with Matchers with Scalatest {

  behavior of "The Position Client"

  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()
  import sys.dispatcher

  val sandboxClient = new PositionClient(OandaEnvironment.SandboxEnvironment)
  val practiceClient = new PositionClient(OandaEnvironment.TradePracticeEnvironment("token"))
  val tradeClient = new PositionClient(OandaEnvironment.TradeEnvironment("token"))

  it must "build the correct request to get all positions" in {
    forAll("accountId") { (accountId: Long) ⇒
      val sandboxReq = sandboxClient.positionsRequest(accountId)
      val practiceReq = practiceClient.positionsRequest(accountId)
      val tradeReq = tradeClient.positionsRequest(accountId)

      // match method
      sandboxReq.method should be(HttpMethods.GET)
      practiceReq.method should be(HttpMethods.GET)
      tradeReq.method should be(HttpMethods.GET)

      // match uri
      sandboxReq.uri.path.toString should be(s"/v1/accounts/$accountId/positions")
      practiceReq.uri.path.toString should be(s"/v1/accounts/$accountId/positions")
      tradeReq.uri.path.toString should be(s"/v1/accounts/$accountId/positions")

      // match headers
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
      practiceReq.headers should contain(Authorization(OAuth2BearerToken("token")))
      tradeReq.headers should contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  it must "build the correct request to get the position for an instrument" in {
    forAll("accountId") { (accountId: Long) ⇒
      val sandboxReq = sandboxClient.positionRequest(accountId, "EUR_USD")
      val practiceReq = practiceClient.positionRequest(accountId, "EUR_USD")
      val tradeReq = tradeClient.positionRequest(accountId, "EUR_USD")

      // match method
      sandboxReq.method should be(HttpMethods.GET)
      practiceReq.method should be(HttpMethods.GET)
      tradeReq.method should be(HttpMethods.GET)

      // match uri
      sandboxReq.uri.path.toString should be(s"/v1/accounts/$accountId/positions/EUR_USD")
      practiceReq.uri.path.toString should be(s"/v1/accounts/$accountId/positions/EUR_USD")
      tradeReq.uri.path.toString should be(s"/v1/accounts/$accountId/positions/EUR_USD")

      // match headers
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
      practiceReq.headers should contain(Authorization(OAuth2BearerToken("token")))
      tradeReq.headers should contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  it must "build the correct request to close the position for an instrument" in {
    forAll("accountId") { (accountId: Long) ⇒
      val sandboxReq = sandboxClient.closePositionRequest(accountId, "EUR_USD")
      val practiceReq = practiceClient.closePositionRequest(accountId, "EUR_USD")
      val tradeReq = tradeClient.closePositionRequest(accountId, "EUR_USD")

      // match method
      sandboxReq.method should be(HttpMethods.DELETE)
      practiceReq.method should be(HttpMethods.DELETE)
      tradeReq.method should be(HttpMethods.DELETE)

      // match uri
      sandboxReq.uri.path.toString should be(s"/v1/accounts/$accountId/positions/EUR_USD")
      practiceReq.uri.path.toString should be(s"/v1/accounts/$accountId/positions/EUR_USD")
      tradeReq.uri.path.toString should be(s"/v1/accounts/$accountId/positions/EUR_USD")

      // match headers
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
      practiceReq.headers should contain(Authorization(OAuth2BearerToken("token")))
      tradeReq.headers should contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  def cleanUp(): Unit = Http().shutdownAllConnectionPools().onComplete(_ ⇒ sys.shutdown())

}
