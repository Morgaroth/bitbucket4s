package io.morgaroth.bitbucketclient.marshalling

import cats.Monad
import cats.data.EitherT
import cats.syntax.either._
import io.circe._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.morgaroth.bitbucketclient._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.language.{higherKinds, implicitConversions}

trait Bitbucket4sMarshalling extends JodaCodec
  with BBUUUIDCodec
  with EntityTypeCodec
  with PullRequestStateCodec
  with MergeStrategyCodec {

  implicit class Extractable(value: JsonObject) {
    def extract[T](implicit decoder: Decoder[T]): Either[Error, T] = decode[T](value.toString)
  }

  object MJson {
    def read[T](str: String)(implicit d: Decoder[T]): Either[Error, T] = decode[T](str)

    def readT[F[_], T](str: String)(implicit d: Decoder[T], m: Monad[F], requestId: RequestId): EitherT[F, BitbucketError, T] =
      EitherT.fromEither(read[T](str).leftMap[BitbucketError](e => BBUnmarshallingError(e.getMessage, requestId.id, e)))

    def write[T](value: T)(implicit d: Encoder[T]): String = Printer.noSpaces.copy(dropNullValues = true).pretty(value.asJson)

    def writePretty[T](value: T)(implicit d: Encoder[T]): String = printer.pretty(value.asJson)
  }

  // keep all special settings with method write above
  implicit val printer: Printer = Printer.spaces2.copy(dropNullValues = true)
}

object Bitbucket4sMarshalling extends Bitbucket4sMarshalling

trait CommonDateSerializer {
  def print(d: DateTime): String = {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    formatter.print(d)
  }

  implicit def datetimePrinter(d: DateTime): {} = new {
    def dateString: String = print(d)
  }
}

object CommonDateSerializer extends CommonDateSerializer
