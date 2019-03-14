package io.morgaroth.bitbucketclient.marshalling

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import io.morgaroth.bitbucketclient.models.BBEntityType

trait EntityTypeCodec {
  implicit val branchTypeDecoder: Decoder[BBEntityType] = Decoder.decodeString.emap { raw =>
    BBEntityType.byName.get(raw).toRight(s"$raw isn't known branch type")
  }

  implicit val branchTypeEncoder: Encoder[BBEntityType] = Encoder.instance[BBEntityType](_.repr.asJson)
}

object EntityTypeCodec extends EntityTypeCodec