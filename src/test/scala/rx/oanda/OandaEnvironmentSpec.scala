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

package rx.oanda

import OandaEnvironment._
import akka.http.scaladsl.model.headers.{OAuth2BearerToken, Authorization}
import org.scalatest._

class OandaEnvironmentSpec extends FlatSpec with Matchers {

  behavior of "The Oanda Environment"

  it must "provide correct request headers based on the environment" in {
    OandaEnvironment.SandboxEnvironment.headers should be (unixTime :: gzipEncoding :: Nil)
    OandaEnvironment.TradePracticeEnvironment("key").headers should be (unixTime :: gzipEncoding :: Authorization(OAuth2BearerToken("key")) :: Nil)
    OandaEnvironment.TradeEnvironment("key").headers should be (unixTime :: gzipEncoding :: Authorization(OAuth2BearerToken("key")) :: Nil)
  }

}
