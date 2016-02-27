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

package rx.oanda.events

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{HostConnectionPool, ServerBinding}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, HttpRequest}
import akka.stream.scaladsl.{Source, Flow}
import akka.http.scaladsl.server.Directives._
import akka.stream.{Materializer, ActorMaterializer}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import rx.oanda.OandaEnvironment.ConnectionPool

import scala.concurrent.Future
import scala.util.Try

trait FakeEventEndpoints extends FlatSpec with BeforeAndAfterAll  {

  implicit val system = ActorSystem("fake-account-endpoints")
  implicit val mat = ActorMaterializer()

  import system.dispatcher

  val route =
    path("v1" / "events") {
      complete {
        HttpEntity.Chunked(ContentTypes.`application/json`,
          Source(List(
            """{"transaction":{"id":176403879,"accountId":6765103,"time":"1453326442000000","type":"MARKET_ORDER_CREATE","instrument":"EUR_USD","units":2,"side":"buy","price":1.25325,"pl":0,"interest":0,"accountBalance":100000,"tradeOpened":{"id":176403879,"units":2}}}""",
            """{"transaction":{"id":176403886,"accountId":6765103,"time":"1453326442000000","type":"STOP_ORDER_CREATE","instrument":"EUR_USD","units":2,"side":"buy","price":1,"expiry":1398902400,"reason":"CLIENT_REQUEST"}}""",
            """{"transaction":{"id":176403886,"accountId":6765103,"time":"1453326442000000","type":"LIMIT_ORDER_CREATE","instrument":"EUR_USD","units":2,"side":"buy","price":1,"expiry":1398902400,"reason":"CLIENT_REQUEST"}}""",
            """{"transaction":{"id":176403882,"accountId":6765103,"time":"1453326442000000","type":"MARKET_IF_TOUCHED_ORDER_CREATE","instrument":"EUR_USD","units":2,"side":"buy","price":1,"expiry":1398902400,"reason":"CLIENT_REQUEST"}}""",
            """{"transaction":{"id":176403883,"accountId":6765103,"time":"1453326442000000","type":"ORDER_UPDATE","instrument":"EUR_USD","units":3,"price":1,"expiry":1398902400,"orderId":176403880,"reason":"REPLACES_ORDER"}}""",
            """{"transaction":{"id":176403881,"accountId":6765103,"time":"1453326442000000","type":"ORDER_CANCEL","orderId":176403880,"reason":"CLIENT_REQUEST"}}""",
            """{"transaction":{"id":175685908,"accountId":2610411,"time":"1453326442000000","type":"ORDER_FILLED","instrument":"EUR_USD","units":2,"side":"buy","price":1.3821,"pl":0,"interest":0,"accountBalance":100000,"orderId":175685907,"tradeOpened":{"id":175685908,"units":2}}}""",
            """{"transaction":{"id":176403884,"accountId":6765103,"time":"1453326442000000","type":"TRADE_UPDATE","instrument":"EUR_USD","units":2,"stopLossPrice":1.1,"tradeId":176403879}}""",
            """{"transaction":{"id":176403885,"accountId":6765103,"time":"1453326442000000","type":"TRADE_CLOSE","instrument":"EUR_USD","units":2,"side":"sell","price":1.25918,"pl":0.0119,"interest":0,"accountBalance":100000.0119,"tradeId":176403879}}""",
            """{"heartbeat":{"time":"1391114831000000"}}""",
            """{"transaction":{"id":176403885,"accountId":6765103,"time":"1453326442000000","type":"MIGRATE_TRADE_CLOSE","instrument":"EUR_USD","units":2,"side":"sell","price":1.25918,"pl":0.0119,"interest":0,"accountBalance":100000.0119,"tradeId":176403879}}""",
            """{"transaction":{"id":175685908,"accountId":2610411,"time":"1453326442000000","type":"MIGRATE_TRADE_OPEN","instrument":"EUR_USD","units":2,"side":"buy","price":1.3821,"tradeOpened":{"id":175685908,"units":2}}}""",
            """{"transaction":{"id":175685954,"accountId":1491998,"time":"1453326442000000","type":"TAKE_PROFIT_FILLED","units":10,"tradeId":175685930,"instrument":"EUR_USD","side":"sell","price":1.38231,"pl":0.0001,"interest":0,"accountBalance":100000.0001}}""",
            """{"transaction":{"id":175685918,"accountId":1403479,"time":"1453326442000000","type":"STOP_LOSS_FILLED","units":10,"tradeId":175685917,"instrument":"EUR_USD","side":"sell","price":1.3821,"pl":-0.0003,"interest":0,"accountBalance":99999.9997}}""",
            """{"transaction":{"id":175739353,"accountId":1491998,"time":"1453326442000000","type":"TRAILING_STOP_FILLED","units":10,"tradeId":175739352,"instrument":"EUR_USD","side":"sell","price":1.38137,"pl":-0.0009,"interest":0,"accountBalance":99999.9992}}""",
            """{"transaction":{"id":175739360,"accountId":1491998,"time":"1453326442000000","type":"MARGIN_CALL_ENTER"}}""",
            """{"transaction":{"id":175739360,"accountId":1491998,"time":"1453326442000000","type":"MARGIN_CALL_EXIT"}}""",
            """{"transaction":{"id":176403889,"accountId":6765103,"time":"1453326442000000","type":"MARGIN_CLOSEOUT","instrument":"EUR_USD","units":2,"side":"sell","price":1.25918,"pl":0.0119,"interest":0,"accountBalance":100000.0119,"tradeId":176403879}}""",
            """{"transaction":{"id":175739360,"accountId":1491998,"time":"1453326442000000","type":"SET_MARGIN_RATE","rate":0.02}}""",
            """{"transaction":{"id":176403878,"accountId":6765103,"time":"1453326442000000","type":"TRANSFER_FUNDS","amount":100000,"accountBalance":100000,"reason":"CLIENT_REQUEST"}}""",
            """{"transaction":{"id":175739363,"accountId":1491998,"time":"1453326442000000","type":"DAILY_INTEREST","instrument":"EUR_USD","interest":10.0414,"accountBalance":99999.9992}}""",
            """{"transaction":{"id":175739369,"accountId":1491998,"time":"1453326442000000","type":"FEE","amount":-10.0414,"accountBalance":99999.9992,"reason":"FUNDS"}}"""
          )).map(ChunkStreamPart.apply))
      }
    }

  var bindingFuture: Future[ServerBinding] = _

  implicit val connectionPool: ConnectionPool = new ConnectionPool {
    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
      Http().cachedHostConnectionPool[T]("localhost", 8004).log("connection")
  }

  override protected def beforeAll(): Unit = {
    bindingFuture = Http().bindAndHandle(route, "localhost", 8004)
  }

  override protected def afterAll(): Unit = bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())


}
