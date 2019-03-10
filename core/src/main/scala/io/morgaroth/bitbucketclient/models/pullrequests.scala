package io.morgaroth.bitbucketclient.models

import java.util.UUID

import org.joda.time.DateTime

case class BBPullRequestLinks(
                               self: BBLink,
                               html: BBLink,
                               decline: BBLink,
                               approve: BBLink,
                               merge: BBLink,
                               diff: BBLink,
                               statuses: BBLink,
                               commits: BBLink,
                               comments: BBLink,
                               activity: BBLink,
                             )

case class BBPullRequestBranch(
                                name: String,

                              )

case class BBPullRequestMergeCommit(
                                     hash: String,
                                   )


case class BBPRSideSpec(
                         commit: BBCommit,
                         repository: BBRepository,
                         branch: BBPullRequestBranch
                       )

case class BBPullRequest(
                          description: String,
                          title: String,
                          `type`: String,
                          close_source_branch: Boolean,
                          id: Long,
                          destination: BBPRSideSpec,
                          created_on: DateTime,
                          summary: BBPullrequestSummary,
                          source: BBPRSideSpec,
                          state: BBPullRequestState,
                          author: BBUser,
                          merge_commit: Option[BBCommit],
                          links: BBPullRequestLinks,
                        )

case class BBPullRequestCompleteInfo(
                                      description: String,
                                      title: String,
                                      `type`: String,
                                      close_source_branch: Boolean,
                                      id: Long,
                                      destination: BBPRSideSpec,
                                      created_on: String,
                                      summary: BBPullrequestSummary,
                                      source: BBPRSideSpec,
                                      state: BBPullRequestState,
                                      author: BBUser,
                                      merge_commit: Option[BBCommit],
                                      links: BBPullRequestLinks,
                                      reviewers: List[BBPullRequestReviewer]
                                    )

case class BBPullrequestSummary(
                                 raw: String,
                                 markup: String,
                                 `type`: String,
                                 html: String,
                               )

sealed abstract class BBPullRequestState(val name: String)

object BBPullRequestStates {
  val all: Seq[BBPullRequestState] = Seq(Merged, Superseded, Open, Declined)
  val byName: Map[String, BBPullRequestState] = all.map(x => x.name -> x).toMap

  case object Merged extends BBPullRequestState("MERGED")

  case object Superseded extends BBPullRequestState("SUPERSEDED")

  case object Open extends BBPullRequestState("OPEN")

  case object Declined extends BBPullRequestState("DECLINED")

}

case class BBPullRequestReviewer(
                                  username: String,
                                  nickname: String,
                                  account_status: String,
                                  display_name: String,
                                  created_on: String,
                                  uuid: UUID,
                                ) {
  def asUsername = BBUserUsername(username)
}


case class BBPullRequestUpdate(
                                title: String,
                                description: String,
                                reviewers: List[BBUserUsername],
                                close_source_branch: Boolean,
                              )
