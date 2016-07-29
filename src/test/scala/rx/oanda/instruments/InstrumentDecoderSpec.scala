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

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest._

class InstrumentDecoderSpec extends FlatSpec with Matchers {



  it must "parse a list of instruments from valid json" in {
    val json =
      """
        |{
        | "instruments" : [
        |  {
        |   "instrument" : "AUD_CAD",
        |   "displayName" : "AUD\/CAD",
        |   "precision": "0.00001",
        |   "pip" : "0.0001",
        |   "maxTradeUnits" : 10000000,
        |   "minTrailingStop": 5,
        |   "maxTrailingStop": 10000,
        |   "marginRate": 0.02,
        |   "halted": true
        |  },
        |  {
        |   "instrument" : "AUD_CHF",
        |   "displayName" : "AUD\/CHF",
        |   "pip" : "0.0001",
        |   "precision": "0.00001",
        |   "maxTradeUnits" : 10000000,
        |   "maxTrailingStop": 10000,
        |   "minTrailingStop": 5,
        |   "marginRate": 0.02,
        |   "halted": true
        |  }
        | ]
        |}
      """.stripMargin
    decode[Vector[Instrument]](json) should matchPattern {
      case Xor.Right(Vector(
        Instrument("AUD_CAD", "AUD/CAD", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, true),
        Instrument("AUD_CHF", "AUD/CHF", 0.0001, 0.00001, 10000000, 10000, 5, 0.02, true)
      )) ⇒
    }
  }

}
