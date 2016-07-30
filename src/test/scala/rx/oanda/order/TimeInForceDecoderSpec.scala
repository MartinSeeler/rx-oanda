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

package rx.oanda.order

import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest.{FlatSpec, Matchers}

class TimeInForceDecoderSpec extends FlatSpec with Matchers {

  behavior of "The TimeInForce Decoder"

  it can "parse a GTC from valid json" in {
    val json =
      """
        |"GTC"
      """.stripMargin
    decode[TimeInForce](json) should matchPattern {
      case Xor.Right(GTC) => //
    }
  }

  it can "parse a GTD from valid json" in {
    val json =
      """
        |"GTD"
      """.stripMargin
    decode[TimeInForce](json) should matchPattern {
      case Xor.Right(GTD) => //
    }
  }

  it can "parse a GFD from valid json" in {
    val json =
      """
        |"GFD"
      """.stripMargin
    decode[TimeInForce](json) should matchPattern {
      case Xor.Right(GFD) => //
    }
  }

  it can "parse a FOK from valid json" in {
    val json =
      """
        |"FOK"
      """.stripMargin
    decode[TimeInForce](json) should matchPattern {
      case Xor.Right(FOK) => //
    }
  }

  it can "parse a IOC from valid json" in {
    val json =
      """
        |"IOC"
      """.stripMargin
    decode[TimeInForce](json) should matchPattern {
      case Xor.Right(IOC) => //
    }
  }

  it must "fail to parse an unknown time in force from valid json" in {
    val json =
      """
        |"FOO"
      """.stripMargin
    decode[TimeInForce](json) should matchPattern {
      case Xor.Left(DecodingFailure("Unknown time in force called 'FOO'", _)) => //
    }
  }

}
