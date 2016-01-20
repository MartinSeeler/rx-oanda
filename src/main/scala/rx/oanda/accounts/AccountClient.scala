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

package rx.oanda.accounts

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import de.knutwalker.akka.stream.support.CirceStreamSupport
import rx.oanda.OandaEnvironment
import rx.oanda.errors.OandaError
import OandaError._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AccountClient(env: OandaEnvironment)(implicit sys: ActorSystem, mat: ActorMaterializer) {

  private val apiFlow = if (env.authRequired) {
    Http().cachedHostConnectionPoolTls[Any](host = env.apiEndpoint).log("api-connection").detach
  } else {
    Http().cachedHostConnectionPool[Any](host = env.apiEndpoint).log("api-connection").detach
  }

  def account(accountID: Long): Future[Account] = {
    val req = HttpRequest(GET, Uri(s"/v1/accounts/$accountID"), headers = env.headers)
    Source.single(req → 42L).log("request")
      .via(apiFlow).log("response")
      .flatMapConcat {
        case (Success(HttpResponse(StatusCodes.OK, header, entity, _)), _) ⇒
          entity.dataBytes
            .via(Gzip.withMaxBytesPerChunk(65536 * 10).decoderFlow)
            .via(CirceStreamSupport.decode[Account])
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.empty
      }
      .runWith(Sink.head)
  }

  def accounts: Source[BaseAccount, Unit] = {
    val req = HttpRequest(GET, Uri(s"/v1/accounts"), headers = env.headers)
    Source.single(req → 42L).log("request")
      .via(apiFlow).log("response")
      .flatMapConcat {
        case (Success(HttpResponse(StatusCodes.OK, header, entity, _)), _) ⇒
          entity.dataBytes
            .via(Gzip.withMaxBytesPerChunk(65536 * 10).decoderFlow)
            .via(CirceStreamSupport.decode[Vector[BaseAccount]])
            .mapConcat(identity)
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.empty
      }
  }

}
