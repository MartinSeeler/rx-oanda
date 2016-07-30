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

import cats.data.Xor
import io.circe.generic.semiauto._
import io.circe.{Decoder, DecodingFailure}

sealed trait AccountFinancingMode
object AccountFinancingMode {

  implicit val decodeAccountFinancingMode: Decoder[AccountFinancingMode] = Decoder.instance { c ⇒
    c.as[String] flatMap {
      case "NO_FINANCING" => Xor.right(NoFinancing)
      case "SECOND_BY_SECOND" => Xor.right(SecondBySecond)
      case "DAILY" => Xor.right(Daily)
      case otherwise => Xor.left(DecodingFailure(s"Unknown account financing mode called '$otherwise'", c.history))
    }
  }

}

/**
 * No financing is paid/charged for open Trades in the Account.
 */
case object NoFinancing extends AccountFinancingMode

/**
 * Second-by-second financing is paid/charged for open Trades in the Account, both daily and when the the Trade is closed.
 */
case object SecondBySecond extends AccountFinancingMode

/**
 * A full day’s worth of financing is paid/charged for open Trades in the Account daily at 5pm New York time.
 */
case object Daily extends AccountFinancingMode
