package io.morgaroth.bitbucketclient

import cats.Monad
import cats.data.EitherT
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import io.circe.generic.auto._
import io.morgaroth.bitbucketclient.marshalling.Bitbucket4sMarshalling
import io.morgaroth.bitbucketclient.models._
import io.morgaroth.bitbucketclient.query.syntax.SearchQuery._
import io.morgaroth.bitbucketclient.query.{BitbucketRequest, Methods, PRStateQ, SearchQ}

import scala.language.{higherKinds, postfixOps}

trait BitbucketRestAPI[F[_]] extends LazyLogging with Bitbucket4sMarshalling {
  val API = "2.0/"

  implicit def m: Monad[F]

  def config: BitbucketConfig

  private val regGen = BitbucketRequest.forServer(config)
  private val rawRequest = BitbucketRequest.rawGetRequests(config)

  protected def invokeRequest(request: BitbucketRequest)(implicit requestId: RequestId): EitherT[F, BitbucketError, String]

  private def getAllPaginatedResponse[A: Decoder](req: BitbucketRequest, kind: String): EitherT[F, BitbucketError, Vector[A]] = {
    def getAll(nextLink: String, acc: Vector[A]): EitherT[F, BitbucketError, Vector[A]] = {
      implicit val rId: RequestId = RequestId.newOne(s"$kind-next-call")
      logger.debug(s"Invoking next with $nextLink")
      invokeRequest(rawRequest(nextLink)).flatMap(MJson.readT[F, PaginatedResponse[A]]).flatMap { result =>
        result.next.map { nextLink =>
          getAll(nextLink, acc ++ result.values)
        }.getOrElse(EitherT.pure(acc ++ result.values))
      }
    }
    {
      implicit val rId2: RequestId = RequestId.newOne(s"$kind-first-call")
      invokeRequest(req).flatMap(MJson.readT[F, PaginatedResponse[A]]).flatMap { first =>
        first.next.map(nextLink => getAll(nextLink, first.values)).getOrElse(EitherT.pure(first.values))
      }
    }
  }

  private def searchRefs[A: Decoder](repoId: String, kind: String, nameLike: String): EitherT[F, BitbucketError, Vector[A]] = {
    val req = regGen(
      Methods.Get, API + s"repositories/$repoId/refs/$kind",
      Vector(SearchQ("name" ~ nameLike)),
      None)

    getAllPaginatedResponse[A](req, "bitbucket-search-refs")
  }

  /**
    * GET /repositories/{username}/{repo_slug}/pullrequests
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests
    */
  def getPullRequests(repoId: String, statuses: Vector[BBPullRequestState]): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    val req = regGen(Methods.Get, API + s"repositories/$repoId/pullrequests", statuses.map(x => PRStateQ(x)), None)
    getAllPaginatedResponse[BBPullRequest](req, "bitbucket-search-prs-by-state")
  }

  /**
    * GET /repositories/{username}/{repo_slug}/pullrequests
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests
    */
  def getPullRequests(repoOwner: String, repoName: String, statuses: Vector[BBPullRequestState]): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    getPullRequests(s"$repoOwner/$repoName", statuses)
  }


  /**
    * GET /repositories/{username}/{repo_slug}/pullrequests
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests
    */
  def getPullRequests(repoId: String, status: BBPullRequestState, statuses: BBPullRequestState*): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    getPullRequests(repoId, status +: statuses.toVector)
  }

  /**
    * Search for PRs
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/meta/filtering
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests#get
    */
  def searchPullRequests(repoId: String)(
    sourceBranchLike: Option[String] = None,
    states: Vector[BBPullRequestState] = Vector.empty,
  ): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    val req = regGen(Methods.Get, API + s"repositories/$repoId/pullrequests", Vector(SearchQ(Vector(
      sourceBranchLike.map(x => "source.branch.name" ~ x).toList,
      List(BBStateP(states))
    ).flatten.reduce(_ and _))), None)

    getAllPaginatedResponse[BBPullRequest](req, "bitbucket-search-pr-by-text-and-state")
  }

  /**
    * Search for Branches
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/meta/filtering
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/refs/branches
    */
  def searchBranches(repoId: String)(nameLike: String): EitherT[F, BitbucketError, Vector[BBBranch]] = {
    searchRefs[BBBranch](repoId, "branches", nameLike)
  }

  def searchBranches(repoOwner: String, repoName: String)(nameLike: String): EitherT[F, BitbucketError, Vector[BBBranch]] = {
    searchRefs[BBBranch](s"$repoOwner/$repoName", "branches", nameLike)
  }

  /**
    * Search for Tags
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/meta/filtering
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/refs/tags
    */
  def searchTags(repoId: String)(nameLike: String): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    searchRefs(repoId, "tags", nameLike)
  }

  def searchTags(repoOwner: String, repoName: String)(nameLike: String): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    searchRefs(s"$repoOwner/$repoName", "tags", nameLike)
  }

  /** GET /2.0/repositories/{username}/{repo_slug}/pullrequests/{pull_request_id}
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests
    *
    */
  def getPullRequest(repoId: String, pullRequestId: Long): EitherT[F, BitbucketError, BBPullRequestCompleteInfo] = {
    implicit val rId: RequestId = RequestId.newOne("bitbucket-get-pr-by-id")
    val req = regGen(Methods.Get, API + s"repositories/$repoId/pullrequests/$pullRequestId", Vector.empty, None)
    invokeRequest(req).flatMap(MJson.readT[F, BBPullRequestCompleteInfo])
  }

  /** PUT /2.0/repositories/{username}/{repo_slug}/pullrequests/{pull_request_id}
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests/%7Bpull_request_id%7D#put
    *
    */
  def updatePullRequest(repoId: String, pullRequestId: Long)(
    title: String,
    description: String,
    reviewers: Vector[BBUserIdentity],
    closeBranch: Boolean,
  ): EitherT[F, BitbucketError, BBPullRequest] = {
    implicit val rId: RequestId = RequestId.newOne("bitbucket-update-pr")
    val data = BBPullRequestUpdate(title, description, reviewers, closeBranch)
    val req = regGen(Methods.Put, API + s"repositories/$repoId/pullrequests/$pullRequestId", Vector.empty, Some(MJson.write(data)))
    invokeRequest(req).flatMap(MJson.readT[F, BBPullRequest])
  }
}