package io.morgaroth.bitbucketclient.models

import org.joda.time.DateTime

trait BBBranchType {
  def repr: String

}

object BBBranchType {
  val all: Seq[BBBranchType] = Seq(NamedBranch)
  val byName: Map[String, BBBranchType] = all.map(x => x.repr -> x).toMap
}

case object NamedBranch extends BBBranchType {
  override val repr: String = "named_branch"
}

case class BBBranch(
                     heads: Vector[BBCommit],
                     name: String,
                     default_merge_strategy: MergeStrategy,
                     merge_strategies: Vector[MergeStrategy],
                     `type`: BBBranchType,
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
                           `type`: String,
                         )