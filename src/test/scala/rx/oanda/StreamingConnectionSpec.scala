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
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.TestFrameworkInterface.Scalatest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.stream.testkit.javadsl.TestSink
import akka.util.ByteString
import cats.data.Xor
import org.scalatest._
import rx.oanda.accounts.SandboxAccount
import rx.oanda.errors.{InvalidInstrument, OandaException}
import rx.oanda.rates.Price
import rx.oanda.utils.Heartbeat

import scala.concurrent.Future

class StreamingConnectionSpec extends FlatSpec with Matchers with Scalatest {

  behavior of "The StreamingConnection"

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  val serverSource = Http().bind("localhost", 8081)

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/prices"), _, _, _) =>
      Future.successful(HttpResponse(entity = HttpEntity(contentType = ContentTypes.`application/json`,
        data = Source(Vector(
          "{\"tick\":{\"instrument\":\"EUR_USD\",\"time\":\"1453847424597195\",\"bid\":1.08646,\"ask\":1.08668}}",
          "{\"heartbeat\":{\"time\":\"1453849454039260\"}}"
        )).map(x ⇒ ByteString.fromString(x)))))
    case HttpRequest(GET, Uri.Path("/oandaError"), _, _, _) =>
      Future.successful(HttpResponse(status = BadRequest,
        entity = HttpEntity(ContentTypes.`application/json`,
          "{\"code\":46,\"message\":\"Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument\",\"moreInfo\":\"http://developer.oanda.com/docs/v1/troubleshooting/#errors\"}")))
  }

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach(_ handleWithAsyncHandler requestHandler)).run()

  val streamingConnection = new StreamingConnection {
    private[oanda] val streamingConnection = Http().cachedHostConnectionPool[Long]("localhost", 8081)
  }

  it must "parse an oanda error when the status code is not 200" in {
    streamingConnection
      .startStreaming[Long](HttpRequest(GET, "/oandaError"), "anything")
      .runWith(TestSink.probe(system))
      .request(1)
      .expectError(new OandaException(InvalidInstrument("Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument")))
  }

  it must "parse a wrapped price and heartbeat in the correct order" in {
    streamingConnection
      .startStreaming[Price](HttpRequest(GET, "/prices"), "tick")
      .runWith(TestSink.probe(system))
      .request(1)
      .expectNext(Xor.Left(Price("EUR_USD", 1453847424597195L, 1.08646, 1.08668)))
      .request(1)
      .expectNext(Xor.Right(Heartbeat(1453849454039260L)))
      .expectComplete()
  }

  it must "fail when no connection is possible" in {
    new StreamingConnection {
      private[oanda] val streamingConnection = Http().cachedHostConnectionPool[Long]("_", 8081)
    }.startStreaming[Price](HttpRequest(GET, "/prices"), "tick")
      .runWith(TestSink.probe(system))
      .request(1)
      .expectError()
  }

  def cleanUp(): Unit = bindingFuture
    .flatMap(_ ⇒ Http().shutdownAllConnectionPools())
    .onComplete(_ ⇒ system.terminate())

}
