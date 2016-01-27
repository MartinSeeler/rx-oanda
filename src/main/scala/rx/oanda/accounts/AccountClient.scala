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
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import rx.oanda.OandaEnvironment._
import rx.oanda.{ApiConnection, OandaEnvironment}

class AccountClient[A <: OandaEnvironment.Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool[A])
  extends ApiConnection {

  private[oanda] val apiConnection = env.apiFlow[Long]

  private[this] val accountRequest: Long ⇒ HttpRequest =
    x ⇒ HttpRequest(GET, Uri(s"/v1/accounts/$x"), headers = env.headers)

  def account(accountId: Long): Source[Account, Unit] = (accountRequest andThen makeRequest[Account])(accountId)

  def createAccount(currency: Option[String] = None)(implicit ev: A =:= NoAuth): Source[TestAccount, Unit] = {
    Source.empty
  }

  def accounts: Source[BaseAccount, Unit] = {
    val req = HttpRequest(GET, Uri(s"/v1/accounts"), headers = env.headers)
    makeRequest[Vector[BaseAccount]](req).mapConcat(identity)
  }

}
