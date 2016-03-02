package rx.oanda.trades

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{Uri, HttpRequest}
import rx.oanda.utils.QueryHelper._

private[trades] object TradeClientRequests {

  /**
    * Builds the request to get a list of open trades
    *
    * @param accountId  The account id to use.
    * @param maxId      Optional The server will return trades with id less than or equal to this,
    *                   in descending order (for pagination).
    * @param count      Optional Maximum number of open trades to return. Default: 50 Max value: 500
    * @param instrument Optional Retrieve open trades for a specific instrument only Default: all
    * @param ids        Optional A list of ids of specific trades to retrieve.
    *                   Maximum number of ids: 50. No other parameter may be specified with the ids parameter.
    * @return The request to use without headers.
    */
  def tradesRequest(accountId: Long, maxId: Option[Long], count: Option[Int], instrument: Option[String], ids: Seq[Long]): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/accounts/$accountId/trades").withRawQueryString(rawQueryStringOf(optionalParam("maxId", maxId) :: optionalParam("count", count) :: optionalParam("instrument", instrument) :: listParam("ids", ids) :: Nil)))

  /**
    * Builds the request to get a specific trade.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to retrieve.
    * @return The request to use without headers.
    */
  def getTradeRequest(accountId: Long, tradeId: Long): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/accounts/$accountId/trades/$tradeId"))

  /**
    * Builds the request to close a specific trade.
    *
    * @param accountId The account id to use.
    * @param tradeId   The id of the trade to close.
    * @return The request to use without headers.
    */
  def closeTradeRequest(accountId: Long, tradeId: Long): HttpRequest =
    HttpRequest(DELETE, Uri(s"/v1/accounts/$accountId/trades/$tradeId"))
}
