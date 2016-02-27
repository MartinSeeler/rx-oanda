/*
 * Copyright 2015 – 2016 Martin Seeler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rx.oanda.events

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import cats.data.Xor
import rx.oanda.OandaEnvironment._
import rx.oanda._
import rx.oanda.events.EventsClient.accountsQuery
import rx.oanda.utils.Heartbeat

class EventsClient(env: OandaEnvironment)(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool) extends StreamingConnection {

  private[oanda] val streamingConnection = env.streamFlow[Long]

  private[oanda] def eventsStreamReq(accounts: Seq[Long]): HttpRequest = {
    HttpRequest(GET, Uri(s"/v1/events").withRawQueryString(accountsQuery(accounts)), headers = env.headers)
  }

  def liveEventsStream(accounts: Seq[Long]): Source[Xor[OandaEvent, Heartbeat], NotUsed] =
    startStreaming[OandaEvent](eventsStreamReq(accounts), "transaction").log("events")

}

object EventsClient {

  private def accountsQuery(accounts: Seq[Long]): String =
    if (accounts.isEmpty) "" else s"accountIds=${accounts.mkString("%2C")}"

}
