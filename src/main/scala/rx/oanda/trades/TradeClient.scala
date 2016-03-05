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

package rx.oanda.trades

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import rx.oanda.OandaEnvironment.ConnectionPool
import rx.oanda.trades.TradeClientRequests._
import rx.oanda.{ApiConnection, OandaEnvironment}

class TradeClient(env: OandaEnvironment)(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool)
  extends ApiConnection {

  private[oanda] val apiConnection = env.apiFlow[Long]

  /**
    * Get the last `count` trades from all instruments.
    *
    * @param accountId The account id to use.
    * @param count     The number of trades to receive.
    * @return A source which emits up to `count` trades.
    */
  def trades(accountId: Long, count: Int = 50, maxId: Option[Long] = None): Source[Trade, NotUsed] =
    makeRequest[Vector[Trade]](tradesRequest(accountId, maxId, Some(count), None, Nil).withHeaders(env.headers))
      .log("trades").mapConcat(identity).log("trade")

  /**
    * Get the last `count` trades from a specific instrument.
    *
    * @param accountId  The account id to use.
    * @param instrument The instrument code to fetch trades for.
    * @param count      The number of trades to receive.
    * @return A source which emits up to `count` trades of the specified instrument.
    */
  def tradesByInstrument(accountId: Long, instrument: String, count: Int = 50, maxId: Option[Long] = None): Source[Trade, NotUsed] =
    makeRequest[Vector[Trade]](tradesRequest(accountId, maxId, Some(count), Some(instrument), Nil).withHeaders(env.headers))
      .log("trades").mapConcat(identity).log("trade")

  /**
    * Get specific trades by their id.
    *
    * @param accountId The account id to use.
    * @param tradeIds  The ids of the trades to retrieve.
    * @return A source which emits the trades.
    */
  def tradesByIds(accountId: Long, tradeIds: Seq[Long]): Source[Trade, NotUsed] =
    makeRequest[Vector[Trade]](tradesRequest(accountId, None, None, None, tradeIds).withHeaders(env.headers))
      .log("trades").mapConcat(identity).log("trade")

  /**
    * Get a specific trade by id.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to retrieve.
    * @return A source which emits the trade.
    */
  def tradeById(accountId: Long, tradeId: Long): Source[Trade, NotUsed] =
    makeRequest[Trade](getTradeRequest(accountId, tradeId).withHeaders(env.headers)).log("trade")

  /**
    * Sets or updates the stop loss value of a trade.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to update.
    * @param stopLoss  The stop loss value to set (as price).
    * @return A source which emits the updated trade.
    */
  def updateStopLoss(accountId: Long, tradeId: Long, stopLoss: Double): Source[Trade, NotUsed] =
    makeRequest[Trade](modifyTradeRequest(accountId, tradeId, Some(stopLoss), None, None).withHeaders(env.headers)).log("trade")

  /**
    * Removes the stop loss value of a trade.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to remove the stop loss value.
    * @return A source which emits the updated trade.
    */
  def removeStopLoss(accountId: Long, tradeId: Long): Source[Trade, NotUsed] =
    makeRequest[Trade](modifyTradeRequest(accountId, tradeId, Some(0), None, None).withHeaders(env.headers)).log("trade")

  /**
    * Sets or updates the take profit value of a trade.
    *
    * @param accountId  The account id to use.
    * @param tradeId    The id of the trade to update.
    * @param takeProfit The take profit value to set (as price).
    * @return A source which emits the updated trade.
    */
  def updateTakeProfit(accountId: Long, tradeId: Long, takeProfit: Double): Source[Trade, NotUsed] =
    makeRequest[Trade](modifyTradeRequest(accountId, tradeId, None, Some(takeProfit), None).withHeaders(env.headers)).log("trade")

  /**
    * Removes the take profit value of a trade.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to remove the take profit value.
    * @return A source which emits the updated trade.
    */
  def removeTakeProfit(accountId: Long, tradeId: Long): Source[Trade, NotUsed] =
    makeRequest[Trade](modifyTradeRequest(accountId, tradeId, None, Some(0), None).withHeaders(env.headers)).log("trade")

  /**
    * Sets or updates the trailing stop of a trade.
    *
    * @param accountId    The account id to use.
    * @param tradeId      The id of the trade to update.
    * @param trailingStop The trailing stop distance in pips, up to one decimal places.
    * @return A source which emits the updated trade.
    */
  def updateTrailingStop(accountId: Long, tradeId: Long, trailingStop: Double): Source[Trade, NotUsed] =
    makeRequest[Trade](modifyTradeRequest(accountId, tradeId, None, None, Some(trailingStop)).withHeaders(env.headers)).log("trade")

  /**
    * Removes the trailing stop from a trade.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to remove the trailing stop.
    * @return A source which emits the updated trade.
    */
  def removeTrailingStop(accountId: Long, tradeId: Long): Source[Trade, NotUsed] =
    makeRequest[Trade](modifyTradeRequest(accountId, tradeId, None, None, Some(0)).withHeaders(env.headers)).log("trade")

  /**
    * Closes a trade.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to close.
    * @return A source which emits the closed trade informations.
    */
  def closeTrade(accountId: Long, tradeId: Long): Source[TradeClosed, NotUsed] =
    makeRequest[TradeClosed](closeTradeRequest(accountId, tradeId).withHeaders(env.headers)).log("close-trade")

}
