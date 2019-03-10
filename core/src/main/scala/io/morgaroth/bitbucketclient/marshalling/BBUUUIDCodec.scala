package io.morgaroth.bitbucketclient.marshalling

import java.util.UUID

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}

import scala.util.Try


trait BBUUUIDCodec {
  implicit val uuidDecoder: Decoder[UUID] = Decoder.decodeString.emapTry { raw =>
    if (raw.startsWith("{") && raw.endsWith("}") && raw.length == 38) Try(UUID.fromString(raw.substring(1, 37)))
    else Try(UUID.fromString(raw))
  }

  implicit val uuidEncoder: Encoder[UUID] = Encoder.instance((u: UUID) => s"{${u.toString}}".asJson)
}

object BBUUUIDCodec extends BBUUUIDCodec
