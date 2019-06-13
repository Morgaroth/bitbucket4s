package io.morgaroth.bitbucketclient

import io.circe.generic.auto._
import io.morgaroth.bitbucketclient.marshalling.Bitbucket4sMarshalling
import io.morgaroth.bitbucketclient.models._
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class Bitbucket4sMarshallingTest extends FlatSpec with Matchers with Bitbucket4sMarshalling {

  behavior of "Bitbucket4sMarshalling"

  it should "read correctly with merge_commit" in {
    val result = MJson.read[PaginatedResponse[BBPullRequest]](Source.fromResource("sample_prs_with_merge_commit.json").mkString)
    result shouldBe 'right
    result.right.get.values should have size 1

    result.right.get.values.head.merge_commit shouldBe defined
  }

  it should "load tags list" in {
    val result = MJson.read[PaginatedResponse[BBTag]](Source.fromResource("tag_list.json").mkString)
    result shouldBe 'right
  }

  Vector("full_pr_info_1.json", "full_pr_info_2.json").foreach { resourceName =>
    it should s"parse full PR info from $resourceName" in {
      val result = MJson.read[BBPullRequestCompleteInfo](Source.fromResource(resourceName).mkString)
      result shouldBe 'right
    }
  }

  Vector("search_branches_response_1.json", "search_branches_response_2.json", "search_branches_response_3.json").foreach { resourceName =>
    it should s"parse branches search response info from $resourceName" in {
      val result = MJson.read[PaginatedResponse[BBBranch]](Source.fromResource(resourceName).mkString)
      result shouldBe 'right
    }
  }

  Vector("search_prs_response_1.json").foreach { resourceName =>
    it should s"parse prs search response from $resourceName" in {
      val result = MJson.read[PaginatedResponse[BBPullRequest]](Source.fromResource(resourceName).mkString)
      result shouldBe 'right
    }
  }

}
