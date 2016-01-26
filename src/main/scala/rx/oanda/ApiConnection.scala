package rx.oanda

import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.scaladsl.{Flow, Source}
import de.knutwalker.akka.stream.support.CirceStreamSupport
import io.circe.Decoder
import rx.oanda.errors.OandaError._

import scala.util.{Failure, Success, Try}

trait ApiConnection {

  private[oanda] def apiConnections: Flow[(HttpRequest, Long), (Try[HttpResponse], Long), HostConnectionPool]

  private[oanda] def makeRequest[R](req: HttpRequest)(implicit ev: Decoder[R]): Source[R, Unit] =
    Source.single(req → 42L).log("request")
      .via(apiConnections).log("response")
      .flatMapConcat {
        case (Success(HttpResponse(StatusCodes.OK, header, entity, _)), _) ⇒
          entity.dataBytes
            .via(Gzip.decoderFlow)
            .via(CirceStreamSupport.decode[R]).log("decode")
        case (Success(HttpResponse(_, _, entity, _)), _) ⇒ entity.asErrorStream
        case (Failure(e), _) ⇒ Source.failed(e)
        case _ ⇒ Source.empty
      }

}
