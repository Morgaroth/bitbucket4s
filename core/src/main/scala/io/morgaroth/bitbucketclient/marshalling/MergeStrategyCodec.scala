package io.morgaroth.bitbucketclient.marshalling

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import io.morgaroth.bitbucketclient.models.MergeStrategy

trait MergeStrategyCodec {
  implicit val mergeStrategyDecoder: Decoder[MergeStrategy] = Decoder.decodeString.emap { raw =>
    MergeStrategy.byName.get(raw).toRight(s"$raw isn't known merge strategy")
  }

  implicit val mergeStrategyEncoder: Encoder[MergeStrategy] = Encoder.instance[MergeStrategy](_.repr.asJson)
}

