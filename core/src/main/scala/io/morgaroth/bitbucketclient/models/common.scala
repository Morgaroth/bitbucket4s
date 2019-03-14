package io.morgaroth.bitbucketclient.models

import java.util.UUID

case class PaginatedResponse[A](
                                 size: Option[Int],
                                 page: Int,
                                 pagelen: Int,
                                 next: Option[String],
                                 previous: Option[String],
                                 values: Vector[A],
                               )

case class BBLink(href: String)

case class BBMinimalLinks(
                           self: BBLink,
                           html: BBLink,
                         )

case class BBLinksAvatar(
                          self: BBLink,
                          html: BBLink,
                          avatar: BBLink,
                        )

case class BBRepository(
                         uuid: Option[UUID],
                         full_name: String,
                         name: String,
                         `type`: String,
                         links: BBLinksAvatar,
                       )

case class BBCommit(
                     hash: String,
                     `type`: String,
                     links: BBMinimalLinks,
                   )

case class BBAuthor(
                     raw: String,
                     `type`: String,
                     user: Option[BBUser],
                   )

case class BBUser(
                   username: String,
                   nickname: String,
                   display_name: String,
                   account_id: Option[String],
                   `type`: String,
                   links: BBLinksAvatar,
                   uuid: UUID,
                 ) {
  def asUsername = BBUserUsername(username)
}

case class BBUserUsername(username: String)


sealed trait MergeStrategy {
  def repr: String
}

object MergeStrategy {
  val all: Seq[MergeStrategy] = Seq(Merge, Squash, FastForward)
  val byName: Map[String, MergeStrategy] = all.map(x => x.repr -> x).toMap

  case object Merge extends MergeStrategy {
    override val repr: String = "merge_commit"
  }

  case object Squash extends MergeStrategy {
    override val repr: String = "squash"
  }

  case object FastForward extends MergeStrategy {
    override val repr: String = "fast_forward"
  }

}
