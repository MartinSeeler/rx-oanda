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
import de.knutwalker.akka.stream.support.CirceStreamSupport
import io.circe._
import io.circe.generic.semiauto._

trait OandaError {
  def message: String
}

object OandaError {

  implicit val decoderOandaError: Decoder[OandaError] = Decoder.instance { c ⇒
    c.get[Int]("code") flatMap {
      case 1 ⇒ c.as[InvalidArgument]
      case 2 ⇒ c.as[MissingArgument]
      case 3 ⇒ c.as[MissingAuthorization]
      case 4 ⇒ c.as[InvalidAuhtorization]
      case 36 ⇒ c.as[InvalidRange]
      case 40 ⇒ c.as[MalformedQueryString]
      case 45 ⇒ c.as[InvalidTimestamp]
      case 46 ⇒ c.as[InvalidInstrument]
      case 47 ⇒ c.as[ArgumentConflict]
      case 68 ⇒ c.as[RateLimitViolation]
      case otherwise ⇒ c.as[UnknownOandaError]
    }
  }

  implicit class OandaErrorEntityConversion(val entity: HttpEntity) extends AnyVal {

    def asErrorStream = entity.dataBytes.log("bytes", _.utf8String)
      .via(CirceStreamSupport.decode[OandaError]).log("decode")
      .flatMapConcat(oandaError ⇒ Source.failed(OandaException(oandaError))).log("oanda-error")

  }

}

case class UnknownOandaError(code: Int, message: String, moreInfo: String) extends OandaError

object UnknownOandaError {
  implicit val decodeUnknownOandaError: Decoder[UnknownOandaError] = deriveDecoder
}

case class InvalidArgument(message: String) extends OandaError

object InvalidArgument {
  implicit val decodeInvalidArgument: Decoder[InvalidArgument] = deriveDecoder
}

case class MissingArgument(message: String) extends OandaError

object MissingArgument {
  implicit val decodeMissingArgument: Decoder[MissingArgument] = deriveDecoder
}

case class MissingAuthorization(message: String) extends OandaError

object MissingAuthorization {
  implicit val decodeMissingAuthorization: Decoder[MissingAuthorization] = deriveDecoder
}

case class InvalidAuhtorization(message: String) extends OandaError

object InvalidAuhtorization {
  implicit val decodeInvalidAuhtorization: Decoder[InvalidAuhtorization] = deriveDecoder
}

case class RateLimitViolation(message: String) extends OandaError

object RateLimitViolation {
  implicit val decodeRateLimitViolation: Decoder[RateLimitViolation] = deriveDecoder
}

case class InvalidTimestamp(message: String) extends OandaError

object InvalidTimestamp {
  implicit val decodeInvalidTimestamp: Decoder[InvalidTimestamp] = deriveDecoder
}

case class InvalidInstrument(message: String) extends OandaError

object InvalidInstrument {
  implicit val decodeInvalidInstrument: Decoder[InvalidInstrument] = deriveDecoder
}

case class InvalidRange(message: String) extends OandaError

object InvalidRange {
  implicit val decodeInvalidRange: Decoder[InvalidRange] = deriveDecoder
}

case class MalformedQueryString(message: String) extends OandaError

object MalformedQueryString {
  implicit val decodeMalformedQueryString: Decoder[MalformedQueryString] = deriveDecoder
}

case class ArgumentConflict(message: String) extends OandaError

object ArgumentConflict {
  implicit val decodeArgumentConflict: Decoder[ArgumentConflict] = deriveDecoder
}