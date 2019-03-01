package io.morgaroth.bitbucketclient

trait BitbucketError

case class BBRequestingError(description: String, cause: Throwable) extends BitbucketError

case class BBHttpError(statusCode: Int, description: String, errorBody: Option[String]) extends BitbucketError

case class BBMarshallingError(description: String, cause: Throwable) extends BitbucketError

case class BBUnmarshallingError(description: String, cause: Throwable) extends BitbucketError

object BBMonads {

  implicit class StringEncodable(str: String) {
    def toUrlEncoded: String = {
      val result = List(
        "ń", "%C5%84",
        "ć", "%C4%87",
        "ą", "%C4%85"
      ).grouped(2).foldLeft(str) {
        case (value, what :: to :: Nil) => {
          value.replace(what, to)
        }
      }
      result
    }
  }

}