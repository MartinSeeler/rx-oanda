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

package rx.oanda.rates.candles

import io.circe.Decoder
import io.circe.generic.semiauto._

case class BidAskCandle(
  time: Long,
  openBid: Double,
  openAsk: Double,
  highBid: Double,
  highAsk: Double,
  lowBid: Double,
  lowAsk: Double,
  closeBid: Double,
  closeAsk: Double,
  volume: Long,
  complete: Boolean
)

object BidAskCandle {

  implicit val decodeBidAskCandle: Decoder[BidAskCandle] =
    deriveFor[BidAskCandle].decoder

  implicit val decodeBidAskCandles =
    Decoder.instance(_.get[Vector[BidAskCandle]]("candles"))

}
