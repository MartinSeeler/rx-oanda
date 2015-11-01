package io.martinseeler.rxoanda

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.scaladsl.{Sink, Source, Flow}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait OandaApiClient { this: OandaClientConfig =>

  private lazy val apiFlow: Flow[(HttpRequest, Long), (Try[HttpResponse], (Long, Long)), HostConnectionPool] =
    Http().cachedHostConnectionPool[Long](host = "api-sandbox.oanda.com")
      .map{ case (reqTry, reqStart) => (reqTry, (reqStart, System.currentTimeMillis())) }
      .named("api-connection").log("api-connection")


  private[this] def singleRequest(request: HttpRequest, requestHandler: Flow[(HttpRequest, Long), (Try[HttpResponse], (Long, Long)), HostConnectionPool]) =
    Source.single(request.withDefaultHeaders(defaultHeaders) -> System.currentTimeMillis())
      .named("request-flow").log("request")
      .via(requestHandler)
      .mapAsyncUnordered(8) {
        case (Success(resp), (reqStart, reqEnd)) =>
          println(s"Req took ${reqEnd - reqStart}ms")
          resp.entity.dataBytes.via(Gzip.decoderFlow).map(_.utf8String).runWith(Sink.head)
        case (Failure(e), (reqStart, reqEnd)) =>
          println(s"Req failed after ${reqEnd - reqStart}ms")
          Future.failed(e)
      }

}
