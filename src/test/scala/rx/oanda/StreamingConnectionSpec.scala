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
import org.scalatest._
import rx.oanda.accounts.SandboxAccount
import rx.oanda.errors.{InvalidInstrument, OandaException}

import scala.concurrent.Future

class StreamingConnectionSpec extends FlatSpec with Matchers with Scalatest {

  behavior of "The StreamingConnection"

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  val serverSource = Http().bind("localhost", 8080)

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/noGzip"), _, _, _) =>
      Future.successful(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
        "{\"username\":\"keith\",\"password\":\"Rocir~olf4\",\"accountId\":8954947}")))
    case HttpRequest(GET, Uri.Path("/sandboxAccount"), _, _, _) =>
      Future.successful(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
        data = Source.single(ByteString.fromString("{\"username\":\"keith\",\"password\":\"Rocir~olf4\",\"accountId\":8954947}")).via(Gzip.encoderFlow))))
    case HttpRequest(GET, Uri.Path("/oandaError"), _, _, _) =>
      Future.successful(HttpResponse(status = BadRequest,
        entity = HttpEntity(ContentTypes.`application/json`,
          "{\"code\":46,\"message\":\"Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument\",\"moreInfo\":\"http://developer.oanda.com/docs/v1/troubleshooting/#errors\"}")))
  }

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach(_ handleWithAsyncHandler requestHandler)).run()

  val apiConnection = new ApiConnection {
    private[oanda] val apiConnection = Http().cachedHostConnectionPool[Long]("localhost", 8080)
  }

  it must "fail when the content is not gzip encoded" in {
    apiConnection
      .makeRequest[Long](HttpRequest(GET, "/noGzip"))
      .runWith(TestSink.probe(system))
      .request(1)
      .expectError()
  }

  it must "parse an oanda error when the status code is not 200" in {
    apiConnection
      .makeRequest[Long](HttpRequest(GET, "/oandaError"))
      .runWith(TestSink.probe(system))
      .request(1)
      .expectError(new OandaException(InvalidInstrument("Invalid instrument: EUR_USD%2CEUR_GBP is not a valid instrument")))
  }

  it must "parse a sandbox account with gzip encoding" in {
    apiConnection
      .makeRequest[SandboxAccount](HttpRequest(GET, "/sandboxAccount"))
      .runWith(TestSink.probe(system))
      .request(1)
      .expectNext(SandboxAccount("keith", "Rocir~olf4", 8954947L))
  }

  it must "fail when no connection is possible" in {
    new ApiConnection {
      private[oanda] val apiConnection = Http().cachedHostConnectionPool[Long]("_", 8080)
    }.makeRequest[SandboxAccount](HttpRequest(GET, "/sandboxAccount"))
      .runWith(TestSink.probe(system))
      .request(1)
      .expectError()
  }

  def cleanUp(): Unit = bindingFuture
    .flatMap(_ ⇒ Http().shutdownAllConnectionPools())
    .onComplete(_ ⇒ system.shutdown())

}
