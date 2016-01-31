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
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.TestFrameworkInterface.Scalatest
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment
import rx.oanda.OandaEnvironment._

import scala.concurrent.Future
import scala.util.Try

class AccountClientSpec extends FlatSpec with PropertyChecks with Matchers with Scalatest {

  behavior of "The Account Client"

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  val serverSource = Http().bind("localhost", 8082)

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/v1/accounts"), _, _, _) =>
      Future.successful(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
        data = Source.single(
          "{\"accounts\":[{\"accountId\":8954947,\"accountName\":\"Primary\",\"accountCurrency\":\"USD\",\"marginRate\":0.05},{\"accountId\":8954946,\"accountName\":\"Demo\",\"accountCurrency\":\"EUR\",\"marginRate\":0.05}]}"
        ).map(ByteString.fromString).via(Gzip.encoderFlow))))
    case HttpRequest(GET, Uri.Path("/v1/accounts/8954947"), _, _, _) =>
      Future.successful(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
        data = Source.single(
          "{\"accountId\":8954947,\"accountName\":\"Primary\",\"balance\":100000,\"unrealizedPl\":1.1,\"realizedPl\":-2.2,\"marginUsed\":3.3,\"marginAvail\":100000,\"openTrades\":1,\"openOrders\":2,\"marginRate\":0.05,\"accountCurrency\":\"USD\"}"
        ).map(ByteString.fromString).via(Gzip.encoderFlow))))
  }

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach(_ handleWithAsyncHandler requestHandler)).run()

  implicit val WithAuthTestConnectionPool: ConnectionPool[WithAuth] = new ConnectionPool[WithAuth] {
    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
      Http().cachedHostConnectionPool[T]("localhost", 8082).log("connection")
  }

  implicit val NoAuthTestConnectionPool: ConnectionPool[NoAuth] = new ConnectionPool[NoAuth] {
    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
      Http().cachedHostConnectionPool[T]("localhost", 8082).log("connection")
  }

  val noAuthClient = new AccountClient(OandaEnvironment.SandboxEnvironment)
  val authClient = new AccountClient(OandaEnvironment.TradePracticeEnvironment("token"))

  it must "retrieve all accounts with authentication" in {
    authClient.accounts
      .runWith(TestSink.probe[ShortAccount])
      .requestNext(ShortAccount(8954947L, "Primary", "USD", 0.05))
      .requestNext(ShortAccount(8954946L, "Demo", "EUR", 0.05))
      .expectComplete()
  }

  it must "retrieve all accounts without authentication" in {
    noAuthClient.accounts("foobar")
      .runWith(TestSink.probe[ShortAccount])
      .requestNext(ShortAccount(8954947L, "Primary", "USD", 0.05))
      .requestNext(ShortAccount(8954946L, "Demo", "EUR", 0.05))
      .expectComplete()
  }

  it must "retrieve a specific account with and without authentication" in {
    authClient.account(8954947L)
      .runWith(TestSink.probe[Account])
      .requestNext(Account(8954947L, "Primary", 100000, 1.1, -2.2, 3.3, 100000, 1, 2, 0.05, "USD"))
      .expectComplete()
  }

  def cleanUp(): Unit = bindingFuture
    .flatMap(_ ⇒ Http().shutdownAllConnectionPools())
    .onComplete(_ ⇒ system.shutdown())

}
