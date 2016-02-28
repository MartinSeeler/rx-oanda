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
import akka.http.scaladsl.Http.{HostConnectionPool, ServerBinding}
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, HttpRequest}
import akka.http.scaladsl.server.PathMatchers.LongNumber
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.server.Directives._
import akka.stream.{Materializer, ActorMaterializer}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import rx.oanda.OandaEnvironment.ConnectionPool

import scala.concurrent.Future
import scala.util.Try

trait FakePositionEndpoint extends FlatSpec with BeforeAndAfterAll {

  implicit val system = ActorSystem("fake-position-endpoints")
  implicit val mat = ActorMaterializer()

  import system.dispatcher

  val route =
    path("v1" / "accounts" / LongNumber / "positions" / "EUR_USD") { accountId ⇒
      encodeResponseWith(Gzip) {
        get {
          complete {
            HttpEntity("{\"side\":\"sell\",\"instrument\":\"EUR_USD\",\"units\":9,\"avgPrice\":1.3093}").withContentType(ContentTypes.`application/json`)
          }
        } ~
        delete {
          complete {
            HttpEntity("{\"ids\":[12345,12346,12347],\"instrument\":\"EUR_USD\",\"totalUnits\":1234,\"price\":1.2345}").withContentType(ContentTypes.`application/json`)
          }
        }
      }
    } ~
      path("v1" / "accounts" / LongNumber / "positions") { accountId ⇒
        get {
          encodeResponseWith(Gzip) {
            complete {
              HttpEntity("{\"positions\":[{\"instrument\":\"EUR_USD\",\"units\":4741,\"side\":\"buy\",\"avgPrice\":1.3626},{\"instrument\":\"USD_CAD\",\"units\":30,\"side\":\"sell\",\"avgPrice\":1.11563},{\"instrument\":\"USD_JPY\",\"units\":88,\"side\":\"buy\",\"avgPrice\":102.455}]}").withContentType(ContentTypes.`application/json`)
            }
          }
        }
      }

  var bindingFuture: Future[ServerBinding] = _

  implicit val connectionPool: ConnectionPool = new ConnectionPool {
    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
      Http().cachedHostConnectionPool[T]("localhost", 8005).log("connection")
  }

  override protected def beforeAll(): Unit = {
    bindingFuture = Http().bindAndHandle(route, "localhost", 8005)
  }

  override protected def afterAll(): Unit = bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())


}
