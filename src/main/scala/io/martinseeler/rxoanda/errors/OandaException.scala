package io.martinseeler.rxoanda.errors

case class OandaException(oandaError: OandaError) extends Exception {
  override def getMessage: String = oandaError.message
}
