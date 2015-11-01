package io.martinseeler.rxoanda.rates

import akka.actor.Cancellable
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.scaladsl.Source
import io.martinseeler.rxoanda.OandaStreamClient
import io.martinseeler.rxoanda.rates.RateEvent._

trait OandaRatesEndpoint {
  this: OandaStreamClient =>

  private val ratesEndpoint: String = "/v1/prices"

  def getRates(instrumentCodes: List[String]): Source[RateEvent, Cancellable] =
    streamRequest(instrumentsToReq(instrumentCodes))
      .log("rate-json", _.noSpaces)
      .map(rateEventDecoder.decodeJson)
      .log("rate-json-decode")
      .map(_.fold(e => throw e, x => x))
      .log("rate-event")

  private def instrumentsToReq(instrumentCodes: List[String]): HttpRequest = HttpRequest(
    GET, Uri(ratesEndpoint).withQuery(Map("instruments" -> instrumentCodes.mkString(",")))
  )

}
