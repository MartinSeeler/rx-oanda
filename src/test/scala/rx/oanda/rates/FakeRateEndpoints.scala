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
    path("v1" / "candles") {
      encodeResponseWith(Gzip) {
        get {
          parameterMap { params ⇒
            (params.get("count"), params.get("start"), params.get("end")) match {
              case (Some(_), None, None) ⇒ complete {
                params.get("candleFormat") match {
                  case Some("bidask") ⇒ HttpEntity("""{"candles":[{"time":"1455488785000000","openBid":1.1001,"openAsk":1.1005,"highBid":1.202,"highAsk":1.204,"lowBid":1.0001,"lowAsk":1.0005,"closeBid":1.1101,"closeAsk":1.1105,"volume":1337,"complete":true}]}""").withContentType(ContentTypes.`application/json`)
                  case Some("midpoint") ⇒ HttpEntity("""{"candles":[{"time":"1455488785000000","openMid":1.2,"highMid":1.4,"lowMid":1.0,"closeMid":1.1,"volume":1337,"complete":true}]}""")
                  case _ ⇒ fail("Unknown candle type!")
                }
              }
              case (None, Some(_), Some(_)) ⇒ complete {
                params.get("candleFormat") match {
                  case Some("bidask") ⇒ HttpEntity("""{"candles":[{"time":"1455488788000000","openBid":1.2001,"openAsk":1.2005,"highBid":1.302,"highAsk":1.304,"lowBid":1.1001,"lowAsk":1.1005,"closeBid":1.2101,"closeAsk":1.2105,"volume":42,"complete":true}]}""").withContentType(ContentTypes.`application/json`)
                  case Some("midpoint") ⇒ HttpEntity("""{"candles":[{"time":"1455488788000000","openMid":1.3,"highMid":1.5,"lowMid":1.1,"closeMid":1.2,"volume":42,"complete":true}]}""")
                  case _ ⇒ fail("Unknown candle type!")
                }
              }
              case _ ⇒ fail("Illegal combination of arguments")
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
