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
import akka.http.scaladsl.Http.{HostConnectionPool, ServerBinding}
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.{HttpResponse, HttpRequest, ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.ParameterDirectives
import akka.stream.scaladsl.Flow
import akka.stream.{Materializer, ActorMaterializer}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import rx.oanda.OandaEnvironment.{NoAuth, WithAuth, ConnectionPool}

import scala.concurrent.Future
import scala.util.Try

trait FakeRateEndpoints extends FlatSpec with BeforeAndAfterAll {

  implicit val system = ActorSystem("fake-rate-endpoints")
  implicit val mat = ActorMaterializer()

  import system.dispatcher

  val route =
    path("v1" / "instruments") {
      encodeResponseWith(Gzip) {
        get {
          parameterMap { params ⇒
            params.get("instruments").map(x ⇒ x.split("%2C").toList).getOrElse(Nil) match {
              case Nil ⇒ complete {
                HttpEntity("""{"instruments":[{"instrument":"AUD_CAD","displayName":"AUD/CAD","precision":"0.00001","pip":"0.0001","maxTradeUnits":10000000,"minTrailingStop":5,"maxTrailingStop":10000,"marginRate":0.02,"halted":true},{"instrument":"AUD_CHF","displayName":"AUD/CHF","pip":"0.0001","precision":"0.00001","maxTradeUnits":10000000,"maxTrailingStop":10000,"minTrailingStop":5,"marginRate":0.02,"halted":true}]}""").withContentType(ContentTypes.`application/json`)
              }
              case "AUD_CAD" :: Nil ⇒ complete {
                HttpEntity("""{"instruments":[{"instrument":"AUD_CAD","displayName":"AUD/CAD","precision":"0.00001","pip":"0.0001","maxTradeUnits":10000000,"minTrailingStop":5,"maxTrailingStop":10000,"marginRate":0.02,"halted":true}]}""").withContentType(ContentTypes.`application/json`)
              }
              case _ ⇒ complete {
                HttpEntity("""{"instruments":[{"instrument":"AUD_CAD","displayName":"AUD/CAD","precision":"0.00001","pip":"0.0001","maxTradeUnits":10000000,"minTrailingStop":5,"maxTrailingStop":10000,"marginRate":0.02,"halted":true},{"instrument":"AUD_CHF","displayName":"AUD/CHF","pip":"0.0001","precision":"0.00001","maxTradeUnits":10000000,"maxTrailingStop":10000,"minTrailingStop":5,"marginRate":0.02,"halted":true}]}""").withContentType(ContentTypes.`application/json`)
              }
            }
          }
        }
      }
    } ~
    path("v1" / "prices") {
      encodeResponseWith(Gzip) {
        get {
          parameterMap { params ⇒
            (params.get("instruments").map(x ⇒ x.split("%2C").toList), params.get("since")) match {
              case (Some("EUR_USD" :: Nil), _) ⇒ complete {
                HttpEntity("""{"prices":[{"instrument":"EUR_USD","time":"1453847424597195","bid":1.08646,"ask":1.08668}]}""").withContentType(ContentTypes.`application/json`)
              }
              case (_, _) ⇒ complete {
                HttpEntity("""{"prices":[{"instrument":"EUR_USD","time":"1453847424597195","bid":1.08646,"ask":1.08668},{"instrument":"USD_CAD","time":"1453847424597195","bid":1.28646,"ask":1.28668}]}""").withContentType(ContentTypes.`application/json`)
              }
            }
          }
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
      Http().cachedHostConnectionPool[T]("localhost", 8002).log("connection")
  }

  implicit val NoAuthTestConnectionPool: ConnectionPool[NoAuth] = new ConnectionPool[NoAuth] {
    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
      Http().cachedHostConnectionPool[T]("localhost", 8002).log("connection")
  }

  override protected def beforeAll(): Unit = {
    bindingFuture = Http().bindAndHandle(route, "localhost", 8002)
  }

  override protected def afterAll(): Unit = bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())

}
