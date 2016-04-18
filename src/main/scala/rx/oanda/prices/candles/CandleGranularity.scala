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

sealed trait CandleGranularity



object CandleGranularities {

  case object S5 extends CandleGranularity
  case object S10 extends CandleGranularity
  case object S15 extends CandleGranularity
  case object S30 extends CandleGranularity

  case object M1 extends CandleGranularity
  case object M2 extends CandleGranularity
  case object M3 extends CandleGranularity
  case object M4 extends CandleGranularity
  case object M5 extends CandleGranularity
  case object M10 extends CandleGranularity
  case object M15 extends CandleGranularity
  case object M30 extends CandleGranularity

  case object H1 extends CandleGranularity
  case object H2 extends CandleGranularity
  case object H3 extends CandleGranularity
  case object H4 extends CandleGranularity
  case object H6 extends CandleGranularity
  case object H8 extends CandleGranularity
  case object H12 extends CandleGranularity

  case object D extends CandleGranularity
  case object W extends CandleGranularity
  case object M extends CandleGranularity

}
