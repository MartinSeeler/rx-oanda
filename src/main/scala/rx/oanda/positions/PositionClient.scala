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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import rx.oanda.OandaEnvironment._
import rx.oanda.{ApiConnection, OandaEnvironment}

import PositionClientRequests._

class PositionClient(env: OandaEnvironment)(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool)
  extends ApiConnection {

  private[oanda] val apiConnection = env.apiFlow[Long]

  /**
    * Get all open positions for an account.
    *
    * @param accountId The account id to use.
    * @return A stream which emits all currently open positions, if any.
    */
  def positions(accountId: Long): Source[Position, NotUsed] =
    makeRequest[Vector[Position]](positionsRequest(accountId).withHeaders(env.headers))
      .log("positions").mapConcat(identity).log("position")

  /**
    * Gets the position for an instrument, if available.
    *
    * @param accountId  The account id to use.
    * @param instrument The instrument to use for the lookup.
    * @return A source which emits the position for the instrument, if any.
    */
  def positionByInstrument(accountId: Long, instrument: String): Source[Position, NotUsed] =
    makeRequest[Position](positionRequest(accountId, instrument).withHeaders(env.headers)).log("position")

  /**
    * Close an existing position for an instrument.
    *
    * @param accountId  The account id to use.
    * @param instrument The instrument which shall be closed.
    * @return A source which emits the closed position.
    */
  def closePosition(accountId: Long, instrument: String): Source[ClosedPosition, NotUsed] =
    makeRequest[ClosedPosition](closePositionRequest(accountId, instrument).withHeaders(env.headers)).log("position-close")

}
