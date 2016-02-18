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

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.testkit.TestFrameworkInterface.Scalatest
import akka.stream.ActorMaterializer
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment

class RatesClientRequestsSpec extends FlatSpec with PropertyChecks with Matchers with Scalatest {

  behavior of "The Rates Client"

  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()
  import sys.dispatcher

  val sandboxClient = new RatesClient(OandaEnvironment.SandboxEnvironment)
  val practiceClient = new RatesClient(OandaEnvironment.TradePracticeEnvironment("token"))
  val tradeClient = new RatesClient(OandaEnvironment.TradeEnvironment("token"))

  it must "build the correct request to get all instruments" in {
    forAll("accountId") { (accountId: Long) ⇒
      val sandboxReq = sandboxClient.instrumentsRequest(accountId)
      val practiceReq = practiceClient.instrumentsRequest(accountId)
      val tradeReq = tradeClient.instrumentsRequest(accountId)

      // match method
      sandboxReq.method should be(HttpMethods.GET)
      practiceReq.method should be(HttpMethods.GET)
      tradeReq.method should be(HttpMethods.GET)

      // match uri
      sandboxReq.uri.path.toString should be("/v1/instruments")
      practiceReq.uri.path.toString should be("/v1/instruments")
      tradeReq.uri.path.toString should be("/v1/instruments")

      // match querystring
      val sandboxQuery = sandboxReq.uri.query()
      sandboxQuery.toMap should have size 2
      sandboxQuery.get("accountId") shouldBe Some(accountId.toString)
      sandboxQuery.get("fields") shouldBe Some("displayName,halted,interestRate,marginRate,maxTradeUnits,maxTrailingStop,minTrailingStop,pip,precision")

      val practiceQuery = practiceReq.uri.query()
      practiceQuery.toMap should have size 2
      practiceQuery.get("accountId") shouldBe Some(accountId.toString)
      practiceQuery.get("fields") shouldBe Some("displayName,halted,interestRate,marginRate,maxTradeUnits,maxTrailingStop,minTrailingStop,pip,precision")

      val tradeQuery = tradeReq.uri.query()
      tradeQuery.toMap should have size 2
      tradeQuery.get("accountId") shouldBe Some(accountId.toString)
      tradeQuery.get("fields") shouldBe Some("displayName,halted,interestRate,marginRate,maxTradeUnits,maxTrailingStop,minTrailingStop,pip,precision")

      // match headers
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
      practiceReq.headers should contain(Authorization(OAuth2BearerToken("token")))
      tradeReq.headers should contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  it must "build the correct request to get specific instruments" in {
    forAll("accountId") { (accountId: Long) ⇒
      val sandboxReq = sandboxClient.instrumentsRequest(accountId, "EUR_USD" :: "EUR_GBP" :: Nil)
      val practiceReq = practiceClient.instrumentsRequest(accountId, "EUR_USD" :: "EUR_GBP" :: Nil)
      val tradeReq = tradeClient.instrumentsRequest(accountId, "EUR_USD" :: "EUR_GBP" :: Nil)

      // match method
      sandboxReq.method should be(HttpMethods.GET)
      practiceReq.method should be(HttpMethods.GET)
      tradeReq.method should be(HttpMethods.GET)

      // match uri
      sandboxReq.uri.path.toString should be("/v1/instruments")
      practiceReq.uri.path.toString should be("/v1/instruments")
      tradeReq.uri.path.toString should be("/v1/instruments")

      // match querystring
      val sandboxQuery = sandboxReq.uri.query()
      sandboxQuery.toMap should have size 3
      sandboxQuery.get("accountId") shouldBe Some(accountId.toString)
      sandboxQuery.get("fields") shouldBe Some("displayName,halted,interestRate,marginRate,maxTradeUnits,maxTrailingStop,minTrailingStop,pip,precision")
      sandboxQuery.get("instruments") shouldBe Some("EUR_USD,EUR_GBP")

      val practiceQuery = practiceReq.uri.query()
      practiceQuery.toMap should have size 3
      practiceQuery.get("accountId") shouldBe Some(accountId.toString)
      practiceQuery.get("fields") shouldBe Some("displayName,halted,interestRate,marginRate,maxTradeUnits,maxTrailingStop,minTrailingStop,pip,precision")
      practiceQuery.get("instruments") shouldBe Some("EUR_USD,EUR_GBP")

      val tradeQuery = tradeReq.uri.query()
      tradeQuery.toMap should have size 3
      tradeQuery.get("accountId") shouldBe Some(accountId.toString)
      tradeQuery.get("fields") shouldBe Some("displayName,halted,interestRate,marginRate,maxTradeUnits,maxTrailingStop,minTrailingStop,pip,precision")
      tradeQuery.get("instruments") shouldBe Some("EUR_USD,EUR_GBP")

      // match headers
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
      practiceReq.headers should contain(Authorization(OAuth2BearerToken("token")))
      tradeReq.headers should contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  it must "build the correct request to get prices for specific instruments" in {
      val sandboxReq = sandboxClient.pricesReq("EUR_USD" :: "EUR_GBP" :: Nil, None)
      val practiceReq = practiceClient.pricesReq("EUR_USD" :: "EUR_GBP" :: Nil, None)
      val tradeReq = tradeClient.pricesReq("EUR_USD" :: "EUR_GBP" :: Nil, None)

      // match method
      sandboxReq.method should be(HttpMethods.GET)
      practiceReq.method should be(HttpMethods.GET)
      tradeReq.method should be(HttpMethods.GET)

      // match uri
      sandboxReq.uri.path.toString should be("/v1/prices")
      practiceReq.uri.path.toString should be("/v1/prices")
      tradeReq.uri.path.toString should be("/v1/prices")

      // match querystring
      val sandboxQuery = sandboxReq.uri.query()
      sandboxQuery.toMap should have size 1
      sandboxQuery.get("instruments") shouldBe Some("EUR_USD,EUR_GBP")

      val practiceQuery = practiceReq.uri.query()
      practiceQuery.toMap should have size 1
      practiceQuery.get("instruments") shouldBe Some("EUR_USD,EUR_GBP")

      val tradeQuery = tradeReq.uri.query()
      tradeQuery.toMap should have size 1
      tradeQuery.get("instruments") shouldBe Some("EUR_USD,EUR_GBP")

      // match headers
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
      practiceReq.headers should contain(Authorization(OAuth2BearerToken("token")))
      tradeReq.headers should contain(Authorization(OAuth2BearerToken("token")))
  }

  it must "build the correct request to stream prices for specific instruments" in {
    forAll("accountId") { (accountId: Long) ⇒
      val sandboxReq = sandboxClient.pricesStreamRequest(accountId, "EUR_USD" :: "EUR_GBP" :: Nil)
      val practiceReq = practiceClient.pricesStreamRequest(accountId, "EUR_USD" :: "EUR_GBP" :: Nil)
      val tradeReq = tradeClient.pricesStreamRequest(accountId, "EUR_USD" :: "EUR_GBP" :: Nil)

      // match method
      sandboxReq.method should be(HttpMethods.GET)
      practiceReq.method should be(HttpMethods.GET)
      tradeReq.method should be(HttpMethods.GET)

      // match uri
      sandboxReq.uri.path.toString should be("/v1/prices")
      practiceReq.uri.path.toString should be("/v1/prices")
      tradeReq.uri.path.toString should be("/v1/prices")

      val sandboxQuery = sandboxReq.uri.query()
      sandboxQuery.toMap should have size 3
      sandboxQuery.get("accountId") shouldBe Some(accountId.toString)
      sandboxQuery.get("instruments") shouldBe Some("EUR_USD,EUR_GBP")
      sandboxQuery.get("sessionId") shouldBe Some("undefined")

      val practiceQuery = practiceReq.uri.query()
      practiceQuery.toMap should have size 3
      practiceQuery.get("accountId") shouldBe Some(accountId.toString)
      practiceQuery.get("instruments") shouldBe Some("EUR_USD,EUR_GBP")
      practiceQuery.get("sessionId") shouldBe Some("undefined")

      val tradeQuery = tradeReq.uri.query()
      tradeQuery.toMap should have size 3
      tradeQuery.get("accountId") shouldBe Some(accountId.toString)
      tradeQuery.get("instruments") shouldBe Some("EUR_USD,EUR_GBP")
      tradeQuery.get("sessionId") shouldBe Some("undefined")

      // match headers
      sandboxReq.headers shouldNot contain(Authorization(OAuth2BearerToken("token")))
      practiceReq.headers should contain(Authorization(OAuth2BearerToken("token")))
      tradeReq.headers should contain(Authorization(OAuth2BearerToken("token")))
    }
  }

  def cleanUp(): Unit = Http().shutdownAllConnectionPools().onComplete(_ ⇒ sys.terminate())

}
