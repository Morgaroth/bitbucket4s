package io.morgaroth.bitbucketclient

import io.circe.generic.auto._
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class ProjectsJsonFormatsTest extends FlatSpec with Matchers with ProjectsJsonFormats {

  behavior of "ProjectsJsonFormats"

  it should "read correctly with merge_commit" in {
    val result = MJson.read[BBPullRequests](Source.fromResource("sample_prs_with_merge_commit.json").mkString)
    result shouldBe 'right
    result.right.get.values should have size 1

    result.right.get.values.head.merge_commit shouldBe defined
    println(result.right.get.values.head.merge_commit)
  }

  it should "remove fields that has None value in raw print" in {
    MJson.write(BBPullRequestUpdate(Some("some-title"), None)) shouldBe """{"title":"some-title"}"""
  }

  it should "remove fields that has None value in pretty print" in {
    MJson.writePretty(BBPullRequestUpdate(Some("some-title"), None)) shouldBe
      """{
        |  "title" : "some-title"
        |}""".stripMargin
  }
}
