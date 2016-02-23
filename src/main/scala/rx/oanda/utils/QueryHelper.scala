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

  def param[A](key: String, value: A): String = s"$key=$value"

  def optionalParam[A](key: String, value: Option[A]): String = value match {
    case Some(x) ⇒ s"$key=$x"
    case None ⇒ ""
  }

  def listParam[A](key: String, value: Seq[A]): String = value match {
    case Nil ⇒ ""
    case _ ⇒ s"$key=${value.mkString("%2C")}"
  }

  def rawQueryStringOf(params: Seq[String]): String = params.filter(_.nonEmpty).mkString("&")

}
