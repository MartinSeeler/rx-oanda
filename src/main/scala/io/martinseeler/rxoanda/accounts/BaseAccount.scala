package io.martinseeler.rxoanda.accounts

import io.circe.Decoder
import io.circe.generic.semiauto._

case class BaseAccount(
  accountId: Long,
  accountName: String,
  accountCurrency: String,
  marginRate: Double
)

object BaseAccount {

  implicit val decodeBaseAccount: Decoder[BaseAccount] =
    deriveFor[BaseAccount].decoder

  implicit val decodeBaseAccounts =
    Decoder.instance(_.get[Vector[BaseAccount]]("accounts"))

}