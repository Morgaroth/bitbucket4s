package io.morgaroth.bitbucketclient

import java.util.UUID

case class RequestId(id: String, kind: String)

object RequestId {
  def newOne(kind: String) = new RequestId(UUID.randomUUID().toString, kind)
}