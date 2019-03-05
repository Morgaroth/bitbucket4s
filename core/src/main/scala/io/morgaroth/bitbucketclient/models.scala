package io.morgaroth.bitbucketclient

import java.util.UUID

case class BBPullRequests(
                           size: Int,
                           page: Int,
                           pagelen: Int,
                           next: Option[String],
                           previous: Option[String],
                           values: Vector[BBPullRequest]
                         )

case class BBUSer(
                   username: String,
                   display_name: String,
                   account_id: String,
                   nickname: String,
                   `type`: String,
                   uuid: UUID,
                   links: BBLinks,
                 )

case class BBLink(href: String)

case class BBLinks(
                    self: BBLink,
                    html: BBLink,
                    avatar: Option[BBLink],
                  )

case class BBCommitLinks(
                          self: BBLink,
                          html: BBLink,
                          avatar: Option[BBLink],
                        )

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

case class BBPullRequestCommit(
                                hash: String,
                                `type`: String,
                                links: BBCommitLinks,
                              )

case class BBPullRequestRepo(
                              uuid: Option[UUID],
                              full_name: String,
                              name: String,
                              `type`: String,
                              links: BBLinks,
                            )

case class BBPRSideSpec(
                         commit: BBPullRequestCommit,
                         repository: BBPullRequestRepo,
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
                          author: BBUSer,
                          merge_commit: Option[BBPullRequestCommit],
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
                                      author: BBUSer,
                                      merge_commit: Option[BBPullRequestCommit],
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
                                  //                                  account_status: String,
                                  display_name: String,
                                  //                                  website: String,
                                  //                                  created_on: String,
                                  uuid: UUID,
                                )


case class BBPullRequestUpdate(
                                title: String,
                                description: String,
                                reviewers: List[BBPullRequestReviewer]
                              )