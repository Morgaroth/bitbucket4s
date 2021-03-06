package io.morgaroth.bitbucketclient.models

import java.util.UUID

import cats.syntax.option._

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
                         `type`: BBEntityType,
                         links: BBLinksAvatar,
                       )

case class BBCommit(
                     hash: String,
                     `type`: BBEntityType,
                     links: BBMinimalLinks,
                   )

case class BBAuthor(
                     raw: String,
                     `type`: BBEntityType,
                     user: Option[BBUser],
                   )

case class BBUser(
                   nickname: String,
                   display_name: String,
                   account_id: Option[String],
                   `type`: BBEntityType,
                   links: BBLinksAvatar,
                   uuid: UUID,
                 ) {
  def asUserId: BBUserIdentity = BBUserIdentity(uuid)
}

case class BBUserUsername(username: String)

case class BBUserIdentity(uuid: Option[UUID], nickname: Option[String])

object BBUserIdentity {
  def apply(userID: UUID) = new BBUserIdentity(userID.some, none)

  def uuid(userID: String) = BBUserIdentity(UUID.fromString(userID).some, none)

  def nickname(nickname: String) = BBUserIdentity(none, nickname.some)
}

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
