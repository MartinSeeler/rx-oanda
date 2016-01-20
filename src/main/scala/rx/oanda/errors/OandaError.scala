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

package rx.oanda.errors

import akka.http.scaladsl.model.HttpEntity
import akka.stream.scaladsl.Source
import cats.data.Xor
import cats.data.Xor.{Left, Right}
import io.circe._
import io.circe.generic.semiauto._
import io.circe.parse._

trait OandaError {
  def message: String
}

object OandaError {

  implicit val decoderOandaError: Decoder[OandaError] = Decoder.instance { c ⇒
    c.downField("code").as[Int] flatMap {
      case 1 ⇒ c.as[InvalidArgument]
      case 2 ⇒ c.as[MissingArgument]
      case 3 ⇒ c.as[MissingAuthorization]
      case 4 ⇒ c.as[InvalidAuhtorization]
      case 68 ⇒ c.as[RateLimitViolation]
      case otherwise ⇒ Xor.left(DecodingFailure(s"Unknown OandaError with code $otherwise", c.history))
    }
  }

  implicit class OandaErrorEntityConversion(val entity: HttpEntity) extends AnyVal {

    def asErrorStream = entity.dataBytes
      .map(_.utf8String)
      .map(decode[OandaError])
      .flatMapConcat {
        case Left(decodeError) ⇒ Source.failed(decodeError)
        case Right(oandaError) ⇒ Source.failed(OandaException(oandaError))
      }

  }

}

case class InvalidArgument(message: String) extends OandaError
object InvalidArgument {
  implicit val decodeInvalidArgument: Decoder[InvalidArgument] = deriveFor[InvalidArgument].decoder
}

case class MissingArgument(message: String) extends OandaError
object MissingArgument {
  implicit val decodeMissingArgument: Decoder[MissingArgument] = deriveFor[MissingArgument].decoder
}

case class MissingAuthorization(message: String) extends OandaError
object MissingAuthorization {
  implicit val decodeMissingAuthorization: Decoder[MissingAuthorization] = deriveFor[MissingAuthorization].decoder
}

case class InvalidAuhtorization(message: String) extends OandaError
object InvalidAuhtorization {
  implicit val decodeInvalidAuhtorization: Decoder[InvalidAuhtorization] = deriveFor[InvalidAuhtorization].decoder
}

case class RateLimitViolation(message: String) extends OandaError
object RateLimitViolation {
  implicit val decodeRateLimitViolation: Decoder[RateLimitViolation] = deriveFor[RateLimitViolation].decoder
}
