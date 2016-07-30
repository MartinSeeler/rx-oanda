package rx.oanda.order

import cats.data.Xor
import io.circe.{Decoder, DecodingFailure}

sealed trait OrderState
object OrderState {

  implicit val decodeOrderState: Decoder[OrderState] = Decoder.instance { c ⇒
    c.as[String] flatMap {
      case "PENDING" => Xor.right(Pending)
      case "FILLED" => Xor.right(Filled)
      case "TRIGGERED" => Xor.right(Triggered)
      case "CANCELLED" => Xor.right(Cancelled)
      case otherwise => Xor.left(DecodingFailure(s"Unknown order state called '$otherwise'", c.history))
    }
  }


}

/** The Order is currently pending execution. */
case object Pending extends OrderState

/** The Order has been filled. */
case object Filled extends OrderState

/** The Order has been triggered. */
case object Triggered extends OrderState

/** The Order has been cancelled. */
case object Cancelled extends OrderState
