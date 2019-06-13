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
                          `type`: BBEntityType,
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
                                      `type`: BBEntityType,
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
                                      reviewers: Vector[BBPullRequestReviewer]
                                    )

case class BBPullrequestSummary(
                                 raw: String,
                                 markup: String,
                                 `type`: BBEntityType,
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
                                  account_status: Option[String],
                                  display_name: String,
                                  uuid: UUID,
                                ) {
  def asUserId: BBUserIdentity = BBUserIdentity(uuid)
}


case class BBPullRequestUpdate(
                                title: String,
                                description: String,
                                reviewers: Vector[BBUserIdentity],
                                close_source_branch: Boolean,
                              )
