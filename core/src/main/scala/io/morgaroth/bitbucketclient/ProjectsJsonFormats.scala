package io.morgaroth.bitbucketclient

import java.util.UUID

import cats.Monad
import cats.data.EitherT
import cats.syntax.either._
import io.circe._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.language.{higherKinds, implicitConversions}
import scala.util.Try

trait ProjectsJsonFormats extends JodaCodec with BBUUUIDCodec {

  implicit class Extractable(value: JsonObject) {
    def extract[T](implicit decoder: Decoder[T]): Either[Error, T] = decode[T](value.toString)
  }

  object MJson {
    def read[T](str: String)(implicit d: Decoder[T]): Either[Error, T] = decode[T](str)

    def readT[F[_], T](str: String)(implicit d: Decoder[T], m: Monad[F]): EitherT[F, BitbucketError, T] =
      EitherT.fromEither(read[T](str).leftMap[BitbucketError](e => BBUnmarshallingError(e.getMessage, e)))

    def write[T](value: T)(implicit d: Encoder[T]): String = Printer.noSpaces.copy(dropNullValues = true).pretty(value.asJson)

    def writePretty[T](value: T)(implicit d: Encoder[T]): String = printer.pretty(value.asJson)
  }

  // keep all special settings with method write above
  implicit val printer: Printer = Printer.spaces2.copy(dropNullValues = true)
}

object ProjectsJsonFormats extends ProjectsJsonFormats

trait JodaCodec {

  implicit val jodaDateDecoder: Decoder[DateTime] = Decoder.instance { cursor =>
    cursor.focus.map {
      // String
      case json if json.isString =>
        tryParserDatetime(json.asString.get, DecodingFailure("DateTime", cursor.history))
      // Number
      case json if json.isNumber => json.asNumber match {
        // Long
        case Some(num) if num.toLong.isDefined => Right(new DateTime(num.toLong.get))
        // unknown
        case _ => Left(DecodingFailure("DateTime", cursor.history))
      }
    }.getOrElse {
      // focus return None
      Left(DecodingFailure("DateTime", cursor.history))
    }
  }

  private val alternativeDatePatterns = List(DateTimeFormat.forPattern("yyyy-dd-MM'T'HH:mm:ss.SSSZZ"))

  private def tryParserDatetime(input: String, error: DecodingFailure): Either[DecodingFailure, DateTime] = {
    alternativeDatePatterns.foldLeft(Try(new DateTime(input))) {
      case (acc, patt) => acc.recoverWith { case _ => Try(patt.parseDateTime(input)) }
    }.map(Right(_)).getOrElse(Left(error))
  }

  implicit val jodaDateEncoder: Encoder[DateTime] = Encoder.instance(CommonDateSerializer.print(_).asJson)
}

object JodaCodec extends JodaCodec

trait BBUUUIDCodec {
  implicit val uuidDecoder: Decoder[UUID] = Decoder.decodeString.emapTry { raw =>
    if (raw.startsWith("{") && raw.endsWith("}") && raw.length == 38) Try(UUID.fromString(raw.substring(1, 37)))
    else Try(UUID.fromString(raw))
  }

  implicit val uuidEncoder: Encoder[UUID] = Encoder.instance((u: UUID) => s"{${u.toString}}".asJson)
}

object BBUUUIDCodec extends BBUUUIDCodec


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
