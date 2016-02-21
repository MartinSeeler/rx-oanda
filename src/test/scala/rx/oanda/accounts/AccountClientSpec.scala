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

import akka.stream.testkit.scaladsl.TestSink
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import rx.oanda.OandaEnvironment

class AccountClientSpec extends FlatSpec with PropertyChecks with Matchers with FakeAccountEndpoints {

  behavior of "The AccountClient"

  val noAuthClient = new AccountClient(OandaEnvironment.SandboxEnvironment)
  val authClient = new AccountClient(OandaEnvironment.TradePracticeEnvironment("token"))

  it must "retrieve all accounts with authentication" in {
    authClient.allAccounts
      .runWith(TestSink.probe[ShortAccount])
      .requestNext(ShortAccount(8954947L, "Primary", "USD", 0.05))
      .requestNext(ShortAccount(8954946L, "Demo", "EUR", 0.05))
      .expectComplete()
  }

  it must "retrieve all accounts without authentication" in {
    noAuthClient.allAccounts("foobar")
      .runWith(TestSink.probe[ShortAccount])
      .requestNext(ShortAccount(8954947L, "Primary", "USD", 0.05))
      .requestNext(ShortAccount(8954946L, "Demo", "EUR", 0.05))
      .expectComplete()
  }

  it must "retrieve a specific account with and without authentication" in {
    authClient.accountById(8954947L)
      .runWith(TestSink.probe[Account])
      .requestNext(Account(8954947L, "Primary", 100000, 1.1, -2.2, 3.3, 100000, 1, 2, 0.05, "USD"))
      .expectComplete()
  }

}
