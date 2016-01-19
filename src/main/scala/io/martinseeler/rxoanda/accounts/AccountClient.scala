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

package io.martinseeler.rxoanda.accounts

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import cats.data.Xor.{Left, Right}
import io.circe.Json
import io.circe.parse._
import io.martinseeler.rxoanda.OandaEnvironment
import io.martinseeler.rxoanda.errors.OandaError._
import io.martinseeler.rxoanda.accounts.Account._
import io.martinseeler.rxoanda.accounts.BaseAccount._

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
            .map(_.utf8String).map(decode[Account])
            .flatMapConcat {
              case Left(decodeError) ⇒ Source.failed(decodeError)
              case Right(account) ⇒ Source.single(account)
            }
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
            .map(_.utf8String).log("utf-8").map(decode[Vector[BaseAccount]])
            .flatMapConcat {
              case Left(decodeError) ⇒ Source.failed(decodeError)
              case Right(accounts) ⇒ Source(accounts)
            }
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.empty
      }
  }

}
