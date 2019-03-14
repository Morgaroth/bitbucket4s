package io.morgaroth.bitbucketclient.models

import org.joda.time.DateTime

trait BBEntityType {
  def repr: String

}

object BBEntityType {
  val all: Seq[BBEntityType] = Seq(Repository, NamedBranch, Branch, Commit, PullRequest, Tag, Rendered, User, Author)
  val byName: Map[String, BBEntityType] = all.map(x => x.repr -> x).toMap

  abstract class BBEntityTypeBase(val repr: String) extends BBEntityType

  case object Repository extends BBEntityTypeBase("repository")

  case object NamedBranch extends BBEntityTypeBase("named_branch")

  case object Branch extends BBEntityTypeBase("branch")

  case object Commit extends BBEntityTypeBase("commit")

  case object PullRequest extends BBEntityTypeBase("pullrequest")

  case object Tag extends BBEntityTypeBase("tag")

  case object Rendered extends BBEntityTypeBase("rendered")

  case object User extends BBEntityTypeBase("user")

  case object Author extends BBEntityTypeBase("author")

}

case class BBBranch(
                     heads: Option[Vector[BBCommit]],
                     name: String,
                     default_merge_strategy: MergeStrategy,
                     merge_strategies: Vector[MergeStrategy],
                     `type`: BBEntityType,
                     links: BBLinksCommits,
                     target: BBBranchTarget,
                   )

case class BBLinksCommits(
                           self: BBLink,
                           html: BBLink,
                           commits: BBLink,
                         )

case class BBBranchTarget(
                           hash: String,
                           repository: BBRepository,
                           author: BBAuthor,
                           parents: Vector[BBCommit],
                           date: DateTime,
                           message: String,
                           `type`: BBEntityType,
                         )