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

import java.nio.ByteBuffer

import akka.stream.stage
import akka.stream.stage.{Context, PushPullStage, SyncDirective, TerminationDirective}
import akka.util.ByteString
import io.circe.Json
import io.circe.jawn.CirceSupportParser._
import jawn.AsyncParser

import scala.annotation.tailrec
import scala.language.implicitConversions

final class StreamParser(parser: AsyncParser[Json]) extends PushPullStage[ByteString, Json] {
  private[this] var parsedJsons  : List[Json]           = Nil
  private[this] var unparsedJsons: Iterator[ByteBuffer] = Iterator()

  def onPush(elem: ByteString, ctx: stage.Context[Json]): SyncDirective = {
    if (parsedJsons.nonEmpty) {
      pushAlreadyParsedJson(ctx)
    } else if (unparsedJsons.isEmpty) {
      parseLoop(elem.asByteBuffers.iterator, ctx)
    } else {
      unparsedJsons ++= elem.asByteBuffers.iterator
      parseLoop(unparsedJsons, ctx)
    }
  }

  def onPull(ctx: Context[Json]): SyncDirective = {
    if (parsedJsons.nonEmpty) {
      pushAlreadyParsedJson(ctx)
    } else {
      parseLoop(unparsedJsons, ctx)
    }
  }

  override def onUpstreamFinish(ctx: Context[Json]): TerminationDirective = {
    ctx.absorbTermination()
  }

  private def pushAlreadyParsedJson(ctx: Context[Json]): SyncDirective = {
    val json = parsedJsons.head
    parsedJsons = parsedJsons.tail
    ctx.push(json)
  }

  private def pushAndBufferJson(jsons: Seq[Json], ctx: Context[Json]): SyncDirective = {
    parsedJsons = jsons.tail.toList
    ctx.push(jsons.head)
  }

  @tailrec
  private[this] def parseLoop(in: Iterator[ByteBuffer], ctx: stage.Context[Json]): SyncDirective = {
    if (in.hasNext) {
      val next = in.next()
      val absorb = parser.absorb(next)
      absorb match {
        case Left(e)      ⇒ ctx.fail(e)
        case Right(jsons) ⇒ jsons.size match {
          case 0 ⇒ parseLoop(in, ctx)
          case 1 ⇒ ctx.push(jsons.head)
          case _ ⇒ pushAndBufferJson({unparsedJsons = in; jsons}, ctx)
        }
      }
    } else {
      if (!ctx.isFinishing) {
        ctx.pull()
      } else {
        val finish = parser.finish()
        finish match {
          case Left(e)      ⇒ ctx.fail(e)
          case Right(jsons) ⇒ jsons.size match {
            case 1 ⇒ ctx.pushAndFinish(jsons.head)
            case 0 ⇒ ctx.finish()
            case _ ⇒ pushAndBufferJson(jsons, ctx)
          }
        }
      }
    }
  }
}
