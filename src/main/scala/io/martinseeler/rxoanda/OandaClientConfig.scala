package io.martinseeler.rxoanda

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.model.headers.{`Accept-Encoding`, RawHeader}
import akka.stream.ActorMaterializer

trait OandaClientConfig {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  private[this] val `X-Accept-Datetime-Format-Unix`: HttpHeader = RawHeader("X-Accept-Datetime-Format", "UNIX")

  val defaultHeaders = List(
    `X-Accept-Datetime-Format-Unix`,
    `Accept-Encoding`(gzip, deflate)
  )

}
