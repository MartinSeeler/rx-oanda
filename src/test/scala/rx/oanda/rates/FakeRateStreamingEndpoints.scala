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
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Source, Flow}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import rx.oanda.OandaEnvironment.{ConnectionPool, NoAuth, WithAuth}

import scala.concurrent.Future
import scala.util.Try

trait FakeRateStreamingEndpoints extends FlatSpec with BeforeAndAfterAll {

  implicit val system = ActorSystem("fake-rate-streaming-endpoints")
  implicit val mat = ActorMaterializer()

  import system.dispatcher

  val route =
    path("v1" / "prices") {
      get {
        complete {
          HttpEntity.Chunked(ContentTypes.`application/json`,
            Source(List(
              """{"tick":{"instrument":"AUD_CAD","time":"1391114828000000","bid":0.98114,"ask":0.98139}}""",
              """{"tick":{"instrument":"AUD_CHF","time":"1391114828000000","bid":0.79353,"ask":0.79382}}""",
              """{"tick":{"instrument":"AUD_CHF","time":"1391114831000000","bid":0.79355,"ask":0.79387}}""",
              """{"heartbeat":{"time":"1391114831000000"}}""",
              """{"tick":{"instrument":"AUD_CHF","time":"1391114831000000","bid":0.79357,"ask":0.79390}}""",
              """{"tick":{"instrument":"AUD_CAD","time":"1391114834000000","bid":0.98112,"ask":0.98138}}"""
            )).map(ChunkStreamPart.apply)
          )
        }
      }
    }

  var bindingFuture: Future[ServerBinding] = _

  implicit val WithAuthTestConnectionPool: ConnectionPool[WithAuth] = new ConnectionPool[WithAuth] {
    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
      Http().cachedHostConnectionPool[T]("localhost", 8003).log("connection")
  }

  implicit val NoAuthTestConnectionPool: ConnectionPool[NoAuth] = new ConnectionPool[NoAuth] {
    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
      Http().cachedHostConnectionPool[T]("localhost", 8003).log("connection")
  }

  override protected def beforeAll(): Unit = {
    bindingFuture = Http().bindAndHandle(route, "localhost", 8003)
  }

  override protected def afterAll(): Unit = bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())

}
