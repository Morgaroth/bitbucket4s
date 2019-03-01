package io.morgaroth.bitbucketclient.query

import java.net.URLEncoder

import io.morgaroth.bitbucketclient.query.syntax.SearchQuery
import io.morgaroth.bitbucketclient.{BBPullRequestState, BitbucketConfig}

import scala.language.implicitConversions

sealed trait ParamQuery {
  def render: String
}

class KVParam(key: String, value: String) extends ParamQuery {
  override def render: String = s"$key=$value"
}

case class PRStateQ(s: BBPullRequestState) extends ParamQuery {
  override def render: String = s"state=${s.name}"
}

case class SearchQ(params: SearchQuery) extends KVParam("q", URLEncoder.encode(params.render, "utf-8"))

trait Method

object Methods {

  object Get extends Method

  object Post extends Method

}

case class BitbucketRequest(
                             authToken: String,
                             method: Method,
                             path: String,
                             query: List[ParamQuery],
                             payload: Option[String],
                           ) {
  lazy val render: String = {
    val base = s"https://api.bitbucket.org/$path"
    if (query.nonEmpty) {
      s"$base?${query.map(_.render).mkString("&")}"
    } else base
  }
}

object BitbucketRequest {
  def forServer(cfg: BitbucketConfig): (Method, String, List[ParamQuery], Option[String]) => BitbucketRequest =
    new BitbucketRequest(cfg.getBasicAuthHeaderValue, _, _, _, _)

  def rawGetRequests(cfg: BitbucketConfig): String => BitbucketRequest = {
    new BitbucketRequest(cfg.getBasicAuthHeaderValue, Methods.Get, _, Nil, None)
  }
}