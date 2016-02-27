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

package rx.oanda.events

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{Uri, HttpRequest}
import rx.oanda.utils.QueryHelper._

private[events] object EventClientRequest {

  def eventStreamRequest(accounts: Seq[Long]): HttpRequest =
    HttpRequest(GET, Uri(s"/v1/events").withRawQueryString(rawQueryStringOf(listParam("accountIds", accounts) :: Nil)))

}
