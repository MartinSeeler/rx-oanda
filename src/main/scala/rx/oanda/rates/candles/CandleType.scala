package rx.oanda.rates.candles

import io.circe.Decoder

trait CandleType {

  type R
  def decoder: Decoder[Vector[R]]
  def uriParam: String

}

object CandleTypes {

  case object Midpoint extends CandleType {
    type R = MidpointCandle
    def decoder: Decoder[Vector[MidpointCandle]] = MidpointCandle.decodeMidpointCandles
    def uriParam: String = "midpoint"
  }

  case object BidAsk extends CandleType {
    type R = BidAskCandle
    def decoder: Decoder[Vector[BidAskCandle]] = BidAskCandle.decodeBidAskCandles
    def uriParam: String = "bidask"
  }

}
