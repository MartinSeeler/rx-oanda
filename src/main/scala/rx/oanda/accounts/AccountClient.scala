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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import rx.oanda.OandaEnvironment._
import rx.oanda.accounts.AccountClientRequests._
import rx.oanda.{ApiConnection, OandaEnvironment}

class AccountClient[A <: OandaEnvironment.Auth](env: OandaEnvironment[A])(implicit sys: ActorSystem, mat: Materializer, A: ConnectionPool[A])
  extends ApiConnection {

  private[oanda] val apiConnection = env.apiFlow[Long]

  /**
    * Get all informations about a specific account.
    *
    * @param accountId The account id of the account to lookup.
    * @return A source which emits a single account information instance if the account is available.
    */
  def accountById(accountId: Long): Source[Account, NotUsed] =
    makeRequest[Account](accountInfoRequest(accountId).withHeaders(env.headers))
      .log("account-by-id")

  /**
    * Get a list of all accounts associated with the provided api key.
    * This method is only available in authenticated environments.
    *
    * @return A source which emits a `ShortAccount` per account associated with this api key, if any.
    */
  def allAccounts(implicit ev: MustHaveAuth[A]): Source[ShortAccount, NotUsed] =
    makeRequest[Vector[ShortAccount]](accountListRequest(None).withHeaders(env.headers))
      .mapConcat(identity)
      .log("accounts")

  /**
    * Get a list of all accounts associated with the provided username.
    * This method is only available in the sandbox environment.
    *
    * @param username The username of the `SandboxAccount`.
    * @return A source which emits a `ShortAccount` per account associated with this username, if any.
    */
  def allAccounts(username: String)(implicit ev: MustNotHaveAuth[A]): Source[ShortAccount, NotUsed] =
    makeRequest[Vector[ShortAccount]](accountListRequest(Some(username)).withHeaders(env.headers))
      .mapConcat(identity)
      .log("accounts")

  /**
    * Create a new sandbox account for testing purpose.
    * This method is only available in the sandbox environment.
    *
    * @param currency Optional name of the home currency for the newly created account.
    * @return A source which emits a single `SandboxAccount` if the process was successful.
    */
  def createTestAccount(currency: Option[String] = None)(implicit ev: MustNotHaveAuth[A]): Source[SandboxAccount, NotUsed] =
    makeRequest[SandboxAccount](createTestAccountRequest(currency).withHeaders(env.headers))
      .log("create-test-account")

}
