package rx.oanda.events

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{Uri, HttpRequest}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import cats.data.Xor
import rx.oanda._
import rx.oanda.OandaEnvironment._
import rx.oanda.events.EventsClient.accountsQuery
import rx.oanda.utils.Heartbeat

class EventsClient[A <: Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool[A]) extends StreamingConnection {

  private[oanda] val streamingConnection = env.streamFlow[Long]

  private[oanda] def eventsStreamReq(accounts: Seq[Long]): HttpRequest = {
    HttpRequest(GET, Uri(s"/v1/events").withRawQueryString(accountsQuery(accounts)), headers = env.headers)
  }

  def liveEventsStream(accounts: Seq[Long]): Source[Xor[OandaEvent, Heartbeat], Unit] =
    startStreaming[OandaEvent](eventsStreamReq(accounts), "transaction").log("events")

}

object EventsClient {

  private def accountsQuery(accounts: Seq[Long]): String =
    if (accounts.isEmpty) "" else s"accountIds=${accounts.mkString("%2C")}"

}
