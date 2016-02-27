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
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.Materializer
import akka.stream.scaladsl._

import scala.util.Try


case class OandaEnvironment(
  name: String,
  apiEndpoint: String,
  streamEndpoint: String,
  token: String
)

object OandaEnvironment {

  trait ConnectionPool {

    def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem):
    Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool]

  }

  object ConnectionPool {

    implicit val connectionPool: ConnectionPool = new ConnectionPool {
      def apply[T](endpoint: String)(implicit mat: Materializer, system: ActorSystem): Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool] =
        Http().cachedHostConnectionPoolHttps[T](host = endpoint).log("connection")
    }

  }

  val unixTime: HttpHeader = RawHeader("X-Accept-Datetime-Format", "UNIX")
  val gzipEncoding: HttpHeader = `Accept-Encoding`(HttpEncodings.gzip)

  implicit final class OandaEnvironmentOps(private val env: OandaEnvironment) extends AnyVal {

    def headers = unixTime :: gzipEncoding :: Authorization(OAuth2BearerToken(env.token)) :: Nil

    def apiFlow[T](implicit A: ConnectionPool, mat: Materializer, sys: ActorSystem) = A.apply[T](env.apiEndpoint)
    def streamFlow[T](implicit A: ConnectionPool, mat: Materializer, sys: ActorSystem) = A.apply[T](env.streamEndpoint)

  }

  /**
    * Recommended for testing with your fxTrade Practice
    * account and your personal access token.
    */
  def TradePracticeEnvironment(token: String) =
    OandaEnvironment("fxTrade Practice", "api-fxpractice.oanda.com", "stream-fxpractice.oanda.com", token)

  /**
    * Recommended for production-ready code to execute
    * with your fxTrade account and your personal access token.
    */
  def TradeEnvironment(token: String) =
    OandaEnvironment("fxTrade", "api-fxtrade.oanda.com", "stream-fxtrade.oanda.com", token)

}

