package io.martinseeler.rxoanda.accounts

import io.circe.Decoder
import io.circe.generic.semiauto._

case class Account(
  accountId: Long,
  accountName: String,
  balance: Double,
  unrealizedPl: Double,
  realizedPl: Double,
  marginUsed: Double,
  marginAvail: Double,
  openTrades: Int,
  openOrders: Int,
  marginRate: Double,
  accountCurrency: String
)

object Account {

  implicit val decodeAccount: Decoder[Account] =
    deriveFor[Account].decoder

}




