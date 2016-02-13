package rx.oanda.rates.candles

sealed trait CandleGranularity



object CandleGranularities {

  case object S5 extends CandleGranularity
  case object S10 extends CandleGranularity
  case object S15 extends CandleGranularity
  case object S30 extends CandleGranularity

  case object M1 extends CandleGranularity
  case object M2 extends CandleGranularity
  case object M3 extends CandleGranularity
  case object M4 extends CandleGranularity
  case object M5 extends CandleGranularity
  case object M10 extends CandleGranularity
  case object M15 extends CandleGranularity
  case object M30 extends CandleGranularity

  case object H1 extends CandleGranularity
  case object H2 extends CandleGranularity
  case object H3 extends CandleGranularity
  case object H4 extends CandleGranularity
  case object H6 extends CandleGranularity
  case object H8 extends CandleGranularity
  case object H12 extends CandleGranularity

  case object D extends CandleGranularity
  case object W extends CandleGranularity
  case object M extends CandleGranularity

}
