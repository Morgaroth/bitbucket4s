package io.morgaroth.bitbucketclient

import com.softwaremill.sttp
import io.morgaroth.bitbucketclient.query.{Method, Methods}

package object sttpbackend {


  implicit def convertJiraMethodToSttpMethod(in: Method): sttp.Method = {
    if (in == Methods.Get) sttp.Method.GET else {
      if (in == Methods.Post) sttp.Method.GET else ???
    }
  }
}
