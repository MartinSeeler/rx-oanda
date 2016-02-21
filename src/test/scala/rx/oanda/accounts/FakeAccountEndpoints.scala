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
import akka.http.scaladsl.Http.{HostConnectionPool, ServerBinding}
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, Materializer}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import rx.oanda.OandaEnvironment.{ConnectionPool, NoAuth, WithAuth}

import scala.concurrent.Future
import scala.util.Try

trait FakeAccountEndpoints extends FlatSpec with BeforeAndAfterAll {

  implicit val system = ActorSystem("fake-account-endpoints")
  implicit val mat = ActorMaterializer()

  import system.dispatcher

  val route =
    path("v1" / "accounts") {
      encodeResponseWith(Gzip) {
        complete {
          HttpEntity("{\"accounts\":[{\"accountId\":8954947,\"accountName\":\"Primary\",\"accountCurrency\":\"USD\",\"marginRate\":0.05},{\"accountId\":8954946,\"accountName\":\"Demo\",\"accountCurrency\":\"EUR\",\"marginRate\":0.05}]}").withContentType(ContentTypes.`application/json`)
        }
      }
    } ~
    path("v1" / "accounts" / LongNumber) { accountId ⇒
      encodeResponseWith(Gzip) {
        complete {
          HttpEntity(s"""{"accountId":$accountId,"accountName":"Primary","balance":100000,"unrealizedPl":1.1,"realizedPl":-2.2,"marginUsed":3.3,"marginAvail":100000,"openTrades":1,"openOrders":2,"marginRate":0.05,"accountCurrency":"USD"}""").withContentType(ContentTypes.`application/json`)
        }
      }
    }


  var bindingFuture: Future[ServerBinding] = _

  implicit val WithAuthTestConnectionPool: ConnectionPool[WithAuth] = new ConnectionPool[WithAuth] {
    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
      Http().cachedHostConnectionPool[T]("localhost", 8001).log("connection")
  }

  implicit val NoAuthTestConnectionPool: ConnectionPool[NoAuth] = new ConnectionPool[NoAuth] {
    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
      Http().cachedHostConnectionPool[T]("localhost", 8001).log("connection")
  }

  override protected def beforeAll(): Unit = {
    bindingFuture = Http().bindAndHandle(route, "localhost", 8001)
  }

  override protected def afterAll(): Unit = bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())


}
