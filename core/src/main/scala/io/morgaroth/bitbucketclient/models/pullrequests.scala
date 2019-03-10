package io.morgaroth.bitbucketclient.models

import java.util.UUID

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
                          created_on: String,
                          summary: Map[String, String],
                          source: BBPRSideSpec,
                          state: String,
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
                                      summary: Map[String, String],
                                      source: BBPRSideSpec,
                                      state: String,
                                      author: BBUser,
                                      merge_commit: Option[BBCommit],
                                      links: BBPullRequestLinks,
                                      reviewers: List[BBPullRequestReviewer]
                                    )

abstract class BBPullRequestState(val name: String)

object BBPullRequestStatuses {

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
