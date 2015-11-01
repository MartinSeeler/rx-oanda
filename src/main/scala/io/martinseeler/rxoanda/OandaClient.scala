package io.martinseeler.rxoanda

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.martinseeler.rxoanda.rates.OandaRatesEndpoint

class OandaClient(sys: ActorSystem, mat: ActorMaterializer) extends OandaClientConfig with OandaStreamClient with OandaRatesEndpoint {

  implicit val materializer: ActorMaterializer = mat
  implicit val system      : ActorSystem       = sys

}
