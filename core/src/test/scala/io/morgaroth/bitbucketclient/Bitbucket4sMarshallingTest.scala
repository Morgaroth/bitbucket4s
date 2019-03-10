package io.morgaroth.bitbucketclient

import io.circe.generic.auto._
import io.morgaroth.bitbucketclient.marshalling.Bitbucket4sMarshalling
import io.morgaroth.bitbucketclient.models.{BBBranch, BBPullRequest, BBTag, PaginatedResponse}
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class Bitbucket4sMarshallingTest extends FlatSpec with Matchers with Bitbucket4sMarshalling {

  behavior of "ProjectsJsonFormats"

  it should "read correctly with merge_commit" in {
    val result = MJson.read[PaginatedResponse[BBPullRequest]](Source.fromResource("sample_prs_with_merge_commit.json").mkString)
    result shouldBe 'right
    result.right.get.values should have size 1

    result.right.get.values.head.merge_commit shouldBe defined
    println(result.right.get.values.head.merge_commit)
  }

  it should "load branches list" in {
    val result = MJson.read[PaginatedResponse[BBBranch]](Source.fromResource("branch_list.json").mkString)
    result shouldBe 'right
  }

  it should "load tags list" in {
    val result = MJson.read[PaginatedResponse[BBTag]](Source.fromResource("tag_list.json").mkString)
    result shouldBe 'right
  }
}