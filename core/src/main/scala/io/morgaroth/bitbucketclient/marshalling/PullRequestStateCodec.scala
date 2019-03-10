package io.morgaroth.bitbucketclient.marshalling

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import io.morgaroth.bitbucketclient.models.{BBPullRequestState, BBPullRequestStates}

trait PullRequestStateCodec {
  implicit val pullRequestStateDecoder: Decoder[BBPullRequestState] = Decoder.decodeString.emap { raw =>
    BBPullRequestStates.byName.get(raw).toRight(s"$raw isn't known pullrequest status")
  }

  implicit val pullRequestStateEncoder: Encoder[BBPullRequestState] = Encoder.instance[BBPullRequestState](_.name.asJson)
}

