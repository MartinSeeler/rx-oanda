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

package rx.oanda.instruments

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import rx.oanda.{ApiConnection, OandaEnvironment}
import rx.oanda.OandaEnvironment.ConnectionPool
import rx.oanda.instruments.InstrumentClientRequests._

class InstrumentClient(env: OandaEnvironment)(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool)
  extends ApiConnection {

  private[oanda] val apiConnection = env.apiFlow[Long]

  /**
    * Get all tradeable instruments (currency pairs, CFDs, and commodities) that are available for trading with the account specified.
    *
    * @param accountId The account id to fetch the list of tradeable instruments for.
    * @return A source which emits all available `Instrument`s.
    */
  def allInstruments(accountId: Long): Source[Instrument, NotUsed] =
    makeRequest[Vector[Instrument]](instrumentsRequest(accountId, Nil).withHeaders(env.headers))
      .log("instruments").mapConcat(identity).log("instrument")

  /**
    * Get a list of tradeable instruments (currency pairs, CFDs, and commodities) that are available for trading with the account specified.
    *
    * @param accountId   The account id to fetch the list of tradeable instruments for.
    * @param instruments A list of instruments that are to be returned.
    *                    If the list is empty, all instruments will be returned.
    * @return A source which emits the requested `Instrument`s.
    */
  def instruments(accountId: Long, instruments: Seq[String]): Source[Instrument, NotUsed] =
    makeRequest[Vector[Instrument]](instrumentsRequest(accountId, instruments).withHeaders(env.headers))
      .log("instruments").mapConcat(identity).log("instrument")

  /**
    * Get a specific instruments (currency pairs, CFDs, and commodities) that is available for trading with the account specified.
    *
    * @param accountId  The account id to fetch the tradeable instrument for.
    * @param instrument Name of the instrument to retrieve history for.
    *                   The value should be one of the available instrument codes from `instruments`.
    * @return A source which emits the single `Instrument`.
    */
  def instrument(accountId: Long, instrument: String): Source[Instrument, NotUsed] =
    makeRequest[Vector[Instrument]](instrumentsRequest(accountId, instrument :: Nil).withHeaders(env.headers))
      .log("instruments").mapConcat(identity).log("instrument")

}
