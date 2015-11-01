package io.martinseeler.rxoanda.instruments

case class Instrument(
  instrument: String,
  displayName: String,
  pip: String,
  precision: String,
  maxTradeUnits: Long,
  maxTrailingStop: Double,
  minTrailingStop: Double,
  marginRate: Double,
  halted: Boolean
)
