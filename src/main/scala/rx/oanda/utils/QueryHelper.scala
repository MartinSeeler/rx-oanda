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

package rx.oanda.utils

object QueryHelper {

  def optionalInstrumentsParam(instruments: Seq[String]): String = instruments match {
    case Nil ⇒ ""
    case _ ⇒ s"instruments=${instruments.mkString("%2C")}"
  }

  def optionalInstrumentParam(instrumentO: Option[String]): String = instrumentO match {
    case Some(instrument) ⇒ s"instrument=$instrument"
    case None ⇒ ""
  }

  def optionalIdsParam(ids: Seq[Long]): String = ids match {
    case Nil ⇒ ""
    case _ ⇒ s"ids=${ids.mkString("%2C")}"
  }

  def optionalMaxIdParam(maxIdO: Option[Long]): String = maxIdO match {
    case Some(id) ⇒ s"maxId=$id"
    case None ⇒ ""
  }

  def optionalSessionIdParam(sessionIdO: Option[String]): String = sessionIdO match {
    case Some(id) ⇒ s"sessionId=$id"
    case None ⇒ ""
  }

  def optionalSinceParam(sinceO: Option[Long]): String = sinceO match {
    case Some(since) ⇒ s"since=$since"
    case None ⇒ ""
  }

  def accountIdParam(accountId: Long): String = s"accountId=$accountId"

  def countParam(count: Int): String = s"count=$count"

  def rawQueryStringOf(params: Seq[String]): String = params.filter(_.nonEmpty).mkString("&")

}
