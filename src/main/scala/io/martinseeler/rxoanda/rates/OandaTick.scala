package io.martinseeler.rxoanda.rates

import io.circe.Decoder
import io.circe.generic.semiauto._

case class OandaTick(instrument: String, time: Long, bid: Double, ask: Double)

object OandaTick {

  implicit val decodeOandaTick: Decoder[OandaTick] = deriveFor[OandaTick].decoder

}
