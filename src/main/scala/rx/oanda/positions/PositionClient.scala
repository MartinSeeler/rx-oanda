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
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import rx.oanda.OandaEnvironment._
import rx.oanda.{ApiConnection, OandaEnvironment}

class PositionClient[A <: Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool[A])
  extends ApiConnection {

  private[oanda] val apiConnection = env.apiFlow[Long]

  private[oanda] def positionsRequest(accountId: Long): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/accounts/$accountId/positions"), headers = env.headers)

  private[oanda] def positionRequest(accountId: Long, instrument: String): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/accounts/$accountId/positions/$instrument"), headers = env.headers)

  private[oanda] def closePositionRequest(accountId: Long, instrument: String): HttpRequest =
    HttpRequest(DELETE, Uri(s"/v1/accounts/$accountId/positions/$instrument"), headers = env.headers)

  def positions(accountId: Long): Source[Position, Unit] =
    makeRequest[Vector[Position]](positionsRequest(accountId)).mapConcat(identity)

  def position(accountId: Long, instrument: String): Source[Position, Unit] =
    makeRequest[Position](positionRequest(accountId, instrument))

  def closePosition(accountId: Long, instrument: String): Source[ClosedPosition, Unit] =
    makeRequest[ClosedPosition](closePositionRequest(accountId, instrument))

}
