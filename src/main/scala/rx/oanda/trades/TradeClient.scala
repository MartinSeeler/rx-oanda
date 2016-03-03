package rx.oanda.trades

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import rx.oanda.{ApiConnection, OandaEnvironment}
import rx.oanda.OandaEnvironment.ConnectionPool

import TradeClientRequests._

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
  def trades(accountId: Long, count: Int = 50): Source[Trade, NotUsed] =
    makeRequest[Vector[Trade]](tradesRequest(accountId, None, Some(count), None, Nil))
      .log("trades").mapConcat(identity).log("trade")

  /**
    * Get specific trades by its id.
    *
    * @param accountId The account id to use.
    * @param tradeIds  The ids of the trades to retrieve.
    * @return A source which emits the trades.
    */
  def tradesByIds(accountId: Long, tradeIds: Seq[Long]): Source[Trade, NotUsed] =
    makeRequest[Vector[Trade]](tradesRequest(accountId, None, None, None, tradeIds))
      .log("trades").mapConcat(identity).log("trade")

  /**
    * Get a specific trade by id.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to retrieve.
    * @return A source which emits the trade.
    */
  def tradeById(accountId: Long, tradeId: Long): Source[Trade, NotUsed] =
    makeRequest[Trade](getTradeRequest(accountId, tradeId)).log("trade")

}
