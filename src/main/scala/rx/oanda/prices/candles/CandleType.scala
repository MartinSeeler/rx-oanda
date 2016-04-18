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

package rx.oanda.prices.candles

import io.circe.Decoder

sealed trait CandleType {

  type R
  def decoder: Decoder[Vector[R]]
  def uriParam: String

}

object CandleTypes {

  type Aux[R0] = CandleType { type R = R0 }

  val Midpoint: Aux[MidpointCandle] = new CandleType {
    type R = MidpointCandle
    def decoder: Decoder[Vector[MidpointCandle]] = MidpointCandle.decodeMidpointCandles
    def uriParam: String = "midpoint"
  }

  val BidAsk: Aux[BidAskCandle] = new CandleType {
    type R = BidAskCandle
    def decoder: Decoder[Vector[BidAskCandle]] = BidAskCandle.decodeBidAskCandles
    def uriParam: String = "bidask"
  }

}
