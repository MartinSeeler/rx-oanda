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
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import cats.data.Xor
import rx.oanda.OandaEnvironment._
import rx.oanda._
import rx.oanda.utils.Heartbeat

import EventClientRequest._

class EventClient(env: OandaEnvironment)(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool) extends StreamingConnection {

  private[oanda] val streamingConnection = env.streamFlow[Long]

  /**
    * Stream all events related to all accounts / a subset of accounts.
    *
    * @param accounts The account ids of the accounts to monitor events for. If empty, all accounts are monitored.

    * @return A stream which emits all events when they occure, as well as heartbeats.
    */
  def liveEvents(accounts: Seq[Long] = Nil): Source[Xor[OandaEvent, Heartbeat], NotUsed] =
    startStreaming[OandaEvent](eventStreamRequest(accounts).withHeaders(env.headers), "transaction").log("events")

}