package io.morgaroth.bitbucketclient.marshalling

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import io.morgaroth.bitbucketclient.models.BBBranchType

import scala.language.{higherKinds, implicitConversions}


trait BranchTypeCodec {
  implicit val branchTypeDecoder: Decoder[BBBranchType] = Decoder.decodeString.emap { raw =>
    BBBranchType.byName.get(raw).toRight(s"$raw isn't known branch type")
  }

  implicit val branchTypeEncoder: Encoder[BBBranchType] = Encoder.instance[BBBranchType](_.repr.asJson)
}

object BranchTypeCodec extends BranchTypeCodec