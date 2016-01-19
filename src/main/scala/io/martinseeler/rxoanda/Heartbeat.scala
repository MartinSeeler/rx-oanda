package io.martinseeler.rxoanda

import io.circe.Decoder
import io.circe.generic.semiauto._

case class Heartbeat(time: Long)

object Heartbeat {
  implicit val decoderHeartbeat: Decoder[Heartbeat] =
    deriveFor[Heartbeat].decoder
}
