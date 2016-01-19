package io.martinseeler.rxoanda.rates

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.io.Framing
import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.data.Xor
import cats.data.Xor.{Left, Right}
import io.circe.Decoder
import io.circe.parse._
import io.martinseeler.rxoanda.errors.OandaError.OandaErrorEntityConversion
import io.martinseeler.rxoanda.{Heartbeat, OandaEnvironment}

import scala.util.{Failure, Success}

object RatesClient {

  def rates(accountID: String, instruments: Seq[String])(implicit env: OandaEnvironment, mat: ActorMaterializer): Source[Xor[Heartbeat, OandaTick], Unit] = {
    val params = Map("accountId" → accountID, "instruments" → instruments.mkString(","))
    val req = HttpRequest(GET, Uri(s"/v1/prices").withQuery(Query(params)), headers = env.headers)
    Source.single(req → 42L).log("request")
      .via(env.streamConnection).log("response")
      .flatMapConcat {
        case (Success(HttpResponse(StatusCodes.OK, header, entity, _)), _) ⇒
          entity.dataBytes.log("data-bytes")
            .via(Framing.delimiter(ByteString("\n"), 1024)).log("frame-delimiter")
            .map(_.utf8String).log("utf8")
            .map(json ⇒ decode(json)(Decoder.decodeXor[Heartbeat, OandaTick]("heartbeat", "tick"))).log("decode-xor")
            .flatMapConcat {
              case Left(e) ⇒ Source.failed(e)
              case Right(x) ⇒ Source.single(x)
            }
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.failed(new Exception("Unknown state in rates"))
      }
  }

}
