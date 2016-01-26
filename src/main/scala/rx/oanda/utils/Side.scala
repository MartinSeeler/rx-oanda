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

package rx.oanda.utils

import cats.data.Xor
import io.circe._

sealed trait Side
case object Buy extends Side
case object Sell extends Side

object Side {

  implicit val decodeSide: Decoder[Side] =
    Decoder.instance { c ⇒
      c.as[String] flatMap {
        case "buy" ⇒ Xor.right(Buy)
        case "sell" ⇒ Xor.right(Sell)
        case otherwise ⇒ Xor.left(DecodingFailure(s"Unknown order side named '$otherwise'", c.history))
      }
    }

}
