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

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, Uri}
import rx.oanda.utils.QueryHelper._

private[instruments] object InstrumentClientRequests {

  /**
    * Builds the rquest to get a list of tradeable instruments (currency pairs, CFDs, and commodities)
    * that are available for trading with the account specified.
    *
    * @param accountId   The account id to fetch the list of tradeable instruments for.
    * @param instruments A list of instrument codes that are to be returned in the response.
    *                    If the list is empty, all instruments will be returned.
    * @return The request to use without headers.
    */
  def instrumentsRequest(accountId: Long, instruments: Seq[String]): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/instruments").withRawQueryString(rawQueryStringOf(param("accountId", accountId) :: listParam("instruments", instruments) :: fieldsQuery :: Nil)))


  val instrumentFields = Seq(
    "displayName",
    "halted",
    "interestRate",
    "marginRate",
    "maxTradeUnits",
    "maxTrailingStop",
    "minTrailingStop",
    "pip",
    "precision"
  )

  val fieldsQuery = s"fields=${instrumentFields.mkString("%2C")}"

}
