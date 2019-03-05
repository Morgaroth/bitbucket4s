package io.morgaroth.bitbucketclient

import cats.Monad
import cats.data.EitherT
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.morgaroth.bitbucketclient.query.syntax.SearchQuery._
import io.morgaroth.bitbucketclient.query.{BitbucketRequest, Methods, PRStateQ, SearchQ}

import scala.language.{higherKinds, postfixOps}

trait BitbucketRestAPI[F[_]] extends LazyLogging with ProjectsJsonFormats {
  val API = "2.0/"

  implicit def m: Monad[F]

  def config: BitbucketConfig

  private val regGen = BitbucketRequest.forServer(config)
  private val rawRequest = BitbucketRequest.rawGetRequests(config)

  protected def invokeRequest(request: BitbucketRequest): EitherT[F, BitbucketError, String]

  /**
    * GET /repositories/{username}/{repo_slug}/pullrequests
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests
    */
  def getPullRequests(repoOwner: String, repoName: String, statuses: List[BBPullRequestState]): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    getPullRequests(s"$repoOwner/$repoName", statuses)
  }

  private def getAllPaginatedPRs(req: BitbucketRequest): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    def getAll(nextLink: String, acc: Vector[BBPullRequest]): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
      logger.info(s"Invoking next with $nextLink")
      invokeRequest(rawRequest(nextLink)).flatMap(MJson.readT[F, BBPullRequests]).flatMap { result =>
        result.next.map { nextLink =>
          getAll(nextLink, acc ++ result.values)
        }.getOrElse(EitherT.pure(acc ++ result.values))
      }
    }

    invokeRequest(req).flatMap(MJson.readT[F, BBPullRequests]).flatMap { first =>
      first.next.map(nextLink => getAll(nextLink, first.values)).getOrElse(EitherT.pure(first.values))
    }
  }

  def getPullRequests(repoId: String, statuses: List[BBPullRequestState]): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    val req = regGen(Methods.Get, API + s"repositories/$repoId/pullrequests", statuses.map(x => PRStateQ(x)), None)
    getAllPaginatedPRs(req)
  }

  /**
    * Search for PRs
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/meta/filtering
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests#get
    */
  def searchPullRequests(repoId: String)(
    sourceBranchLike: Option[String] = None,
    states: List[BBPullRequestState] = List.empty,
  ): EitherT[F, BitbucketError, Vector[BBPullRequest]] = {
    val req = regGen(Methods.Get, API + s"repositories/$repoId/pullrequests", List(SearchQ(List(
      sourceBranchLike.map(x => "source.branch.name" ~ x).toList,
      List(BBStateP(states))
    ).flatten.reduce(_ and _))), None)

    getAllPaginatedPRs(req)
  }

  /** GET /2.0/repositories/{username}/{repo_slug}/pullrequests/{pull_request_id}
    *
    * @see https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests
    *
    */
  def getPullRequest(repoId: String, pullRequestId: Long): EitherT[F, BitbucketError, BBPullRequestCompleteInfo] = {
    val req = regGen(Methods.Get, API + s"repositories/$repoId/pullrequests/$pullRequestId", Nil, None)
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
    reviewers: List[BBPullRequestReviewer],
  ): EitherT[F, BitbucketError, BBPullRequest] = {
    val data = BBPullRequestUpdate(title, description, reviewers)
    val req = regGen(Methods.Put, API + s"repositories/$repoId/pullrequests/$pullRequestId", Nil, Some(MJson.write(data)))
    invokeRequest(req).flatMap(MJson.readT[F, BBPullRequest])
  }
}