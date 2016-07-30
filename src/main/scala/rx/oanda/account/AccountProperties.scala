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

package rx.oanda.account

import io.circe.Decoder
import io.circe.generic.semiauto._

/**
  * @param id           The Account’s identifier.
  * @param mt4AccountID The Account’s associated MT4 Account ID. This field will not be present
  *                     if the Account is not an MT4 account.
  * @param tags         The Account’s tags
  */
case class AccountProperties(id: String, mt4AccountID: Option[Int], tags: Seq[String])

object AccountProperties {
  implicit val decodeAccountProperties: Decoder[AccountProperties] = deriveDecoder
}
