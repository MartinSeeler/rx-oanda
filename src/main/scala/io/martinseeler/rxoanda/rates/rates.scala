package io.martinseeler.rxoanda.rates

import java.io.Serializable

import io.circe._
import io.circe.generic.semiauto._

sealed trait RateEvent extends Product with Serializable

case class RateHeartbeat(time: Long) extends RateEvent
case class Tick(instrument: String, time: Long, bid: Double, ask: Double) extends RateEvent

case class TickWrapper(tick: Tick)
object TickWrapper {
  implicit val tickWrapperDecoder = deriveFor[TickWrapper].decoder
}

case class RateHeartbeatWrapper(heartbeat: RateHeartbeat)
object RateHeartbeatWrapper {
  implicit val heartbeatWrapperDecoder = deriveFor[RateHeartbeatWrapper].decoder
}

object RateEvent {
  val rateEventDecoder: Decoder[RateEvent] = Decoder[TickWrapper].map(x => x.tick) ||| Decoder[RateHeartbeatWrapper].map(x => x.heartbeat)
}
