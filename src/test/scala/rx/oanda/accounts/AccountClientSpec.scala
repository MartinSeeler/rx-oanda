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

package rx.oanda.accounts

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.testkit.TestFrameworkInterface.Scalatest
import akka.stream.ActorMaterializer
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment

class AccountClientSpec extends FlatSpec with PropertyChecks with Matchers with Scalatest {

  behavior of "The Account Client"

  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()
  import sys.dispatcher

  val sandboxClient = new AccountClient(OandaEnvironment.SandboxEnvironment)
  val practiceClient = new AccountClient(OandaEnvironment.TradePracticeEnvironment("token"))
  val tradeClient = new AccountClient(OandaEnvironment.TradeEnvironment("token"))

  it must "build the correct requests to get an account" in {
    forAll("accountId") { (accountId: Long) ⇒
      val sandboxReq = sandboxClient.accountRequest(accountId)
      val practiceReq = practiceClient.accountRequest(accountId)
      val tradeReq = tradeClient.accountRequest(accountId)

      // match method
      sandboxReq.method should be(HttpMethods.GET)
      practiceReq.method should be(HttpMethods.GET)
      tradeReq.method should be(HttpMethods.GET)

      // match uri
      sandboxReq.uri.path.toString should be(s"/v1/accounts/$accountId")
      practiceReq.uri.path.toString should be(s"/v1/accounts/$accountId")
      tradeReq.uri.path.toString should be(s"/v1/accounts/$accountId")

      // match headers
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
      practiceReq.headers should contain(Authorization(OAuth2BearerToken("token")))
      tradeReq.headers should contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  it must "build the correct requests to get all account in authenticated environments" in {
    forAll("accountId") { (accountId: Long) ⇒
      val practiceReq = practiceClient.accountsRequest
      val tradeReq = tradeClient.accountsRequest

      // match method
      practiceReq.method should be(HttpMethods.GET)
      tradeReq.method should be(HttpMethods.GET)

      // match uri
      practiceReq.uri.path.toString should be(s"/v1/accounts")
      tradeReq.uri.path.toString should be(s"/v1/accounts")

      // match headers
      practiceReq.headers should contain(Authorization(OAuth2BearerToken("token")))
      tradeReq.headers should contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  it must "build the correct requests to get all account in non authenticated environments" in {
    forAll("accountId") { (accountId: Long) ⇒
      val sandboxReq = sandboxClient.accountsRequest("foobar")

      sandboxReq.method should be(HttpMethods.GET)
      sandboxReq.uri.path.toString should be(s"/v1/accounts")
      sandboxReq.uri.rawQueryString shouldBe Some("username=foobar")
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  it must "fail to get all accounts with username in authenticated environments" in {
    forAll("accountId") { (accountId: Long) ⇒
      "practiceClient.accountsRequest(\"foobar\")" shouldNot typeCheck
      "tradeClient.accountsRequest(\"foobar\")" shouldNot typeCheck
    }
  }

  it must "fail to get all accounts without username in non-authenticated environments" in {
    forAll("accountId") { (accountId: Long) ⇒
      "sandboxClient.accountsRequest()" shouldNot typeCheck
    }
  }

  it must "build the correct requests to create a sandbox account" in {
    forAll("accountId") { (accountId: Long) ⇒
      val sandboxReq = sandboxClient.createAccountRequest

      sandboxReq.method should be(HttpMethods.POST)
      sandboxReq.uri.path.toString should be(s"/v1/accounts")
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  it must "only allow to create an account when sandbox environment is used" in {
    forAll("accountId") { (accountId: Long) ⇒
      "sandboxClient.createAccountRequest" should compile
      "practiceClient.createAccountRequest" shouldNot typeCheck
      "tradeClient.createAccountRequest" shouldNot typeCheck
    }
  }

  def cleanUp(): Unit = Http().shutdownAllConnectionPools().onComplete(_ ⇒ sys.shutdown())

}
