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
import akka.http.javadsl.model.headers.ContentEncoding
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.testkit.TestFrameworkInterface.Scalatest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.stream.testkit.javadsl.TestSink
import akka.util.ByteString
import org.scalatest._
import rx.oanda.accounts.Account
import rx.oanda.errors.{InvalidInstrument, OandaException}

import scala.concurrent.Future

class ApiConnectionSpec extends FlatSpec with Matchers with Scalatest {

  behavior of "The ApiConnection"

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  val serverSource = Http().bind("localhost", 8080)

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/200noGzip"), _, _, _) =>
      Future.successful(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
        "{\"accountId\":8954947,\"accountName\":\"Primary\",\"balance\":100000,\"unrealizedPl\":1.1,\"realizedPl\":-2.2,\"marginUsed\":3.3,\"marginAvail\":100000,\"openTrades\":1,\"openOrders\":2,\"marginRate\":0.05,\"accountCurrency\":\"USD\"}")))
    case HttpRequest(GET, Uri.Path("/200withGzip"), _, _, _) =>
      Future.successful(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
        data = Source.single(ByteString.fromString("{\"accountId\":8954947,\"accountName\":\"Primary\",\"balance\":100000,\"unrealizedPl\":1.1,\"realizedPl\":-2.2,\"marginUsed\":3.3,\"marginAvail\":100000,\"openTrades\":1,\"openOrders\":2,\"marginRate\":0.05,\"accountCurrency\":\"USD\"}")).via(Gzip.encoderFlow)), headers = List(ContentEncoding.create(gzip))))
    case HttpRequest(GET, Uri.Path("/400noGzip"), _, _, _) =>
      Future.successful(HttpResponse(status = BadRequest,
        entity = HttpEntity(ContentTypes.`application/json`,
          "{\"code\":46,\"message\":\"Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument\",\"moreInfo\":\"http://developer.oanda.com/docs/v1/troubleshooting/#errors\"}")))
    case HttpRequest(GET, Uri.Path("/400withGzip"), _, _, _) =>
      Future.successful(HttpResponse(status = BadRequest,
        entity = HttpEntity(ContentTypes.`application/json`,
          data = Source.single(ByteString.fromString("{\"code\":46,\"message\":\"Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument\",\"moreInfo\":\"http://developer.oanda.com/docs/v1/troubleshooting/#errors\"}")).via(Gzip.encoderFlow)), headers = List(ContentEncoding.create(gzip))))
  }

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach(_ handleWithAsyncHandler requestHandler)).run()

  val apiConnection = new ApiConnection {
    private[oanda] val apiConnection = Http().cachedHostConnectionPool[Long]("localhost", 8080)
  }

  it must "decode an entity when the status code is 200 and no gzip encoding present" in {
    apiConnection
      .makeRequest[Account](HttpRequest(GET, "/200noGzip"))
      .runWith(TestSink.probe(system))
      .requestNext(Account(8954947L, "Primary", 100000, 1.1, -2.2, 3.3, 100000, 1, 2, 0.05, "USD"))
      .expectComplete()
  }

  it must "decode an entity when the status code is 200 and gzip encoding is present" in {
    apiConnection
      .makeRequest[Account](HttpRequest(GET, "/200withGzip"))
      .runWith(TestSink.probe(system))
      .requestNext(Account(8954947L, "Primary", 100000, 1.1, -2.2, 3.3, 100000, 1, 2, 0.05, "USD"))
      .expectComplete()
  }

  it must "decode an oanda error when the status code is not 200 and no gzip encoding present" in {
    apiConnection
      .makeRequest[Long](HttpRequest(GET, "/400noGzip"))
      .runWith(TestSink.probe(system))
      .request(1)
      .expectError(new OandaException(InvalidInstrument("Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument")))
  }

  it must "decode an oanda error when the status code is not 200 and gzip encoding is present" in {
    apiConnection
      .makeRequest[Long](HttpRequest(GET, "/400withGzip"))
      .runWith(TestSink.probe(system))
      .request(1)
      .expectError(new OandaException(InvalidInstrument("Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument")))
  }

  it must "fail when no connection is possible" in {
    new ApiConnection {
      private[oanda] val apiConnection = Http().cachedHostConnectionPool[Long]("_", 8080)
    }.makeRequest[Account](HttpRequest(GET, "/sandboxAccount"))
      .runWith(TestSink.probe(system))
      .request(1)
      .expectError()
  }

  def cleanUp(): Unit = bindingFuture
    .flatMap(_ ⇒ Http().shutdownAllConnectionPools())
    .onComplete(_ ⇒ system.terminate())

}
