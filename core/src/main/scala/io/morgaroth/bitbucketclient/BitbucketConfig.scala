package io.morgaroth.bitbucketclient

import java.util.Base64

import com.typesafe.config.Config

case class BitbucketConfig(login: String, pass: String) {
  assert(login.nonEmpty && pass.nonEmpty, "Bitbucket credentials empty!")

  def getBasicAuthHeaderValue = s"Basic ${Base64.getEncoder.encodeToString(s"$login:$pass".getBytes("utf-8"))}"
}

object BitbucketConfig {
  def fromConfig(config: Config) = new BitbucketConfig(
    config.getString("login"),
    config.getString("password"),
  )
}
