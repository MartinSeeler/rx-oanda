package io.martinseeler.rxoanda

import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.Cancellable
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.scaladsl.{Keep, FlattenStrategy, Source, Flow}
import io.circe.Json
import io.circe.jawn.CirceSupportParser
import jawn.AsyncParser.ValueStream

import scala.util.{Failure, Success, Try}

trait OandaStreamClient {
  this: OandaClientConfig =>

  lazy val streamFlow: Flow[(HttpRequest, Long), (Try[HttpResponse], (Long, Long)), HostConnectionPool] =
    Http().cachedHostConnectionPool[Long](host = "stream-sandbox.oanda.com")
      .map { case (respTry, reqStart) => (respTry, (reqStart, System.currentTimeMillis())) }
      .log("stream-response")

  private[rxoanda] def streamRequest(request: HttpRequest): Source[Json, Cancellable] = {
    val cancelled = new AtomicBoolean(false)
    val cancellable = new Cancellable {
      override def cancel(): Boolean = {
        println("cancel called")
        if (!isCancelled) {
          println("not canceled")
          cancelled.set(true)
        }
        true
      }

      override def isCancelled: Boolean = cancelled.get()
    }
    Source.single(request.withDefaultHeaders(defaultHeaders) -> System.currentTimeMillis())
      .log("stream-request")
      .viaMat(streamFlow)(Keep.both)
      .map {
        case (Success(resp), (reqStart, reqEnd)) =>
          println(s"Req took ${reqEnd - reqStart}ms")
          resp.entity.dataBytes
            .transform(() => new StreamParser(CirceSupportParser.async(ValueStream)))
            .takeWhile(_ => !cancelled.get())
        case (Failure(e), (reqStart, reqEnd)) =>
          println(s"Req failed after ${reqEnd - reqStart}ms")
          cancellable.cancel()
          Source.lazyEmpty
      }.flatten(FlattenStrategy.concat)
      .mapMaterializedValue(_ => cancellable)

  }
}
