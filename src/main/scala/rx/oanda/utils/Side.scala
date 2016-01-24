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
