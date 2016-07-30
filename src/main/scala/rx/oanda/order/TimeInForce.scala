package rx.oanda.order

import cats.data.Xor
import io.circe.{Decoder, DecodingFailure}

/**
  * The time-in-force of an Order. TimeInForce describes how long
  * an Order should remain pending before being automatically
  * cancelled by the execution system.
  */
sealed trait TimeInForce
object TimeInForce {

  implicit val decodeTimeInForce: Decoder[TimeInForce] = Decoder.instance { c ⇒
    c.as[String] flatMap {
      case "GTC" => Xor.right(GTC)
      case "GTD" => Xor.right(GTD)
      case "GFD" => Xor.right(GFD)
      case "FOK" => Xor.right(FOK)
      case "IOC" => Xor.right(IOC)
      case otherwise => Xor.left(DecodingFailure(s"Unknown time in force called '$otherwise'", c.history))
    }
  }

}

/** The Order is “Good unTil Cancelled". */
case object GTC extends TimeInForce

/** The Order is “Good unTil Date” and will be cancelled at the provided time. */
case object GTD extends TimeInForce

/** The Order is “Good For Day” and will be cancelled at 5pm New York time. */
case object GFD extends TimeInForce

/** The Order must be immediately “Filled Or Killed”. */
case object FOK extends TimeInForce

/** The Order must be “Immediatedly paritally filled Or Cancelled”. */
case object IOC extends TimeInForce