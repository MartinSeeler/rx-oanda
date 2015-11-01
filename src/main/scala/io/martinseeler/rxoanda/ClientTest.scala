/*
 * Copyright 2015 Martin Seeler
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

package io.martinseeler.rxoanda

import akka.actor.{Cancellable, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Flow, Source, Sink}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

object ClientTest extends App {

  implicit val system = ActorSystem("oanda-client-test")
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  private val client: OandaClient = new OandaClient(system, mat)

  private val allInstruments: List[String] = List("AU200_AUD", "AUD_CAD", "AUD_CHF", "AUD_HKD", "AUD_JPY", "AUD_NZD", "AUD_SGD", "AUD_USD", "BCO_USD", "CAD_CHF", "CAD_HKD", "CAD_JPY", "CAD_SGD", "CH20_CHF", "CHF_HKD", "CHF_JPY", "CHF_ZAR", "CORN_USD", "DE10YB_EUR", "DE30_EUR", "EU50_EUR", "EUR_AUD", "EUR_CAD", "EUR_CHF", "EUR_CZK", "EUR_DKK", "EUR_GBP", "EUR_HKD", "EUR_HUF", "EUR_JPY", "EUR_NOK", "EUR_NZD", "EUR_PLN", "EUR_SEK", "EUR_SGD", "EUR_TRY", "EUR_USD", "EUR_ZAR", "FR40_EUR", "GBP_AUD", "GBP_CAD", "GBP_CHF", "GBP_HKD", "GBP_JPY", "GBP_NZD", "GBP_PLN", "GBP_SGD", "GBP_USD", "GBP_ZAR", "HK33_HKD", "HKD_JPY", "JP225_USD", "NAS100_USD", "NATGAS_USD", "NL25_EUR", "NZD_CAD", "NZD_CHF", "NZD_HKD", "NZD_JPY", "NZD_SGD", "NZD_USD", "SG30_SGD", "SGD_CHF", "SGD_HKD", "SGD_JPY", "SOYBN_USD", "SPX500_USD", "SUGAR_USD", "TRY_JPY", "UK100_GBP", "UK10YB_GBP", "US2000_USD", "US30_USD", "USB02Y_USD", "USB05Y_USD", "USB10Y_USD", "USB30Y_USD", "USD_CAD", "USD_CHF", "USD_CNH", "USD_CNY", "USD_CZK", "USD_DKK", "USD_HKD", "USD_HUF", "USD_INR", "USD_JPY", "USD_MXN", "USD_NOK", "USD_PLN", "USD_SAR", "USD_SEK", "USD_SGD", "USD_THB", "USD_TRY", "USD_TWD", "USD_ZAR", "WHEAT_USD", "WTICO_USD", "XAG_AUD", "XAG_CAD", "XAG_CHF", "XAG_EUR", "XAG_GBP", "XAG_HKD", "XAG_JPY", "XAG_NZD", "XAG_SGD", "XAG_USD", "XAU_AUD", "XAU_CAD", "XAU_CHF", "XAU_EUR", "XAU_GBP", "XAU_HKD", "XAU_JPY", "XAU_NZD", "XAU_SGD", "XAU_USD", "XAU_XAG", "XCU_USD", "XPD_USD", "XPT_USD", "XXX_AUD", "XXX_CAD", "XXX_CHF", "XXX_EUR", "XXX_GBP", "XXX_HKD", "XXX_JPY", "XXX_SGD", "XXX_USD", "ZAR_JPY")
  val (cancel: Cancellable, completed: Future[Unit]) = client.getRates(List("EUR_USD")).log("rate").toMat(Sink.foreach(println))(Keep.both).run()

  completed.onComplete(_ => system.shutdown())

}
