package rx.oanda.account

import io.circe.Decoder
import io.circe.generic.semiauto._

/**
  * @param id           The Account’s identifier.
  * @param mt4AccountID The Account’s associated MT4 Account ID. This field will not be present
  *                     if the Account is not an MT4 account.
  * @param tags         The Account’s tags
  */
case class AccountProperties(id: String, mt4AccountID: Option[Int], tags: Seq[String])

object AccountProperties {
  implicit val decodeAccountProperties: Decoder[AccountProperties] = deriveDecoder
}
