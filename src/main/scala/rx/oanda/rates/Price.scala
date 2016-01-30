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

package rx.oanda.rates

import io.circe.Decoder
import io.circe.generic.semiauto._

case class Price(instrument: String, time: Long, bid: Double, ask: Double)

object Price {

  implicit val decodePrice: Decoder[Price] =
    deriveFor[Price].decoder

  implicit val decodePrices =
    Decoder.instance(_.get[Vector[Price]]("prices"))

}
