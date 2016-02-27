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

package rx.oanda.accounts

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, Uri}

private[accounts] object AccountClientRequests {

  /**
    * Builds the request to get a list of accounts owned by the user.
    *
    * @return The request to use without headers.
    */
  val accountListRequest: HttpRequest =
    HttpRequest(GET, Uri(s"/v1/accounts"))

  /**
    * Builds the request to get informations about an account.
    *
    * @param accountId The accountId of the account to lookup.
    * @return The request to use without headers.
    */
  def accountInfoRequest(accountId: Long): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/accounts/$accountId"))


}
