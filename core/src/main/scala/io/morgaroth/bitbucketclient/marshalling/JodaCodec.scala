package io.morgaroth.bitbucketclient.marshalling

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, DecodingFailure, Encoder}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.util.Try

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