package io.morgaroth.bitbucketclient.sttpbackend

import cats.Monad
import cats.data.EitherT
import cats.instances.future.catsStdInstancesForFuture
import cats.syntax.either._
import com.softwaremill.sttp._
import com.typesafe.scalalogging.{LazyLogging, Logger}
import io.morgaroth.bitbucketclient._
import io.morgaroth.bitbucketclient.query.BitbucketRequest
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SttpBitbucketAPI(val config: BitbucketConfig, apiConfig: BitbucketRestAPIConfig)(implicit ex: ExecutionContext) extends BitbucketRestAPI[Future] with LazyLogging {

  override implicit val m: Monad[Future] = implicitly[Monad[Future]]

  implicit val backend: SttpBackend[Try, Nothing] = TryHttpURLConnectionBackend()
  private val requestsLogger = Logger(LoggerFactory.getLogger(getClass.getPackage.getName + ".requests"))

  override def invokeRequest(requestData: BitbucketRequest)(implicit requestId: RequestId): EitherT[Future, BitbucketError, String] = {
    val u = requestData.render
    val requestWithoutPayload = sttp.method(requestData.method, uri"$u").headers(
      "Authorization" -> requestData.authToken,
      "Accept" -> "application/json",
      "User-Agent" -> "curl/7.61.0",
    )

    val request = requestData.payload.map { rawPayload =>
      requestWithoutPayload.body(rawPayload).contentType("application/json")
    }.getOrElse(requestWithoutPayload)

    if (apiConfig.debug) logger.debug(s"request to send: $request")
    requestsLogger.info(s"Request ID {}, request: {}, payload:\n{}", requestId, request.body("removed for log"), request.body)

    val response = request
      .send()
      .toEither.leftMap[BitbucketError](BBRequestingError("try-http-backend-left", requestId.id, _))
      .flatMap { response =>
        if (apiConfig.debug) logger.debug(s"received request: $response")
        requestsLogger.info(s"Request ID {}, response: {}, payload:\n{}", requestId, response.copy(rawErrorBody = Right("removed for log")), response.body.fold(identity, identity))
        response.rawErrorBody.leftMap(error => BBHttpError(response.code.intValue(), "http-response-error", requestId.id, requestId.kind, Some(new String(error, "UTF-8"))))
      }

    EitherT.fromEither[Future](response)
  }
}