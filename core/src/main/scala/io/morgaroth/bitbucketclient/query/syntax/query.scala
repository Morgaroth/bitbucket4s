package io.morgaroth.bitbucketclient.query.syntax

import java.net.URLEncoder

import io.morgaroth.bitbucketclient.models.BBPullRequestState
import org.joda.time.DateTime


trait SearchQuery {
  def render: String
}


abstract class BBSearchLogicOperator(j1: SearchQuery, o: String, j2: SearchQuery) extends SearchQuery {
  override lazy val render: String = {
    s"${j1.render} $o ${j2.render}"
  }
}

abstract class BBSearchOperator(j1: String, o: String, j2: String) extends SearchQuery {
  override lazy val render: String = {
    s"$j1 $o $j2"
  }
}

abstract class BBSearchStringOperator(j1: String, o: String, j2: String) extends SearchQuery {
  override lazy val render: String = s"""$j1 $o "$j2""""
}

abstract class BBSearchRawOperator(j1: String, o: String, j2: String) extends SearchQuery {
  override lazy val render: String = {
    s"$j1 $o $j2"
  }
}


case class BBGteString(l: String, r: String) extends BBSearchStringOperator(l, ">=", r)

case class BBGte(l: String, r: String) extends BBSearchOperator(l, ">=", r)

case class BBGtString(l: String, r: String) extends BBSearchStringOperator(l, ">", r)

case class BBGt(l: String, r: String) extends BBSearchOperator(l, ">", r)

case class BBLteString(l: String, r: String) extends BBSearchStringOperator(l, "<=", r)

case class BBLte(l: String, r: String) extends BBSearchOperator(l, "<=", r)

case class BBLtString(l: String, r: String) extends BBSearchStringOperator(l, "<", r)

case class BBLt(l: String, r: String) extends BBSearchOperator(l, "<", r)

case class BBEQString(l: String, r: String) extends BBSearchStringOperator(l, "=", r)

case class BBEQ(l: String, r: String) extends BBSearchOperator(l, "=", r)

case class BB_NEQ_String(l: String, r: String) extends BBSearchStringOperator(l, "!=", r)

case class BB_NEQ(l: String, r: String) extends BBSearchOperator(l, "!=", r)

case class BB_Contain_String(l: String, r: String) extends BBSearchStringOperator(l, "~", r)

case class BB__Not_Contain_String(l: String, r: String) extends BBSearchStringOperator(l, "!~", r)


case class BBOr(j1: SearchQuery, j2: SearchQuery) extends BBSearchLogicOperator(j1, "OR", j2)

case class BBAnd(j1: SearchQuery, j2: SearchQuery) extends BBSearchLogicOperator(j1, "AND", j2)

case class BBGroup(inside: SearchQuery) extends SearchQuery {
  override def render: String = s"(${inside.render})"
}

object SearchQuery {

  implicit class HigherSyntax(left: SearchQuery) {
    def and(right: SearchQuery): SearchQuery = BBAnd(left, right)

    def or(right: SearchQuery): SearchQuery = BBOr(left, right)

  }

  implicit class LowerSyntax(left: String) {

    def gte(right: String): SearchQuery = BBGteString(left, right)

    def lte(right: String): SearchQuery = BBLteString(left, right)

    def gt(right: String): SearchQuery = BBGtString(left, right)

    def lt(right: String): SearchQuery = BBLtString(left, right)

    def gte(right: Long): SearchQuery = BBGte(left, right.toString)

    def lte(right: Long): SearchQuery = BBLte(left, right.toString)

    def gt(right: Long): SearchQuery = BBGt(left, right.toString)

    def lt(right: Long): SearchQuery = BBLt(left, right.toString)

    def gte(right: DateTime): SearchQuery = BBGte(left, right.toString("YYYY-DD-MMTHH:mm:ssZZ"))

    def lte(right: DateTime): SearchQuery = BBLte(left, right.toString("YYYY-DD-MMTHH:mm:ssZZ"))

    def gt(right: DateTime): SearchQuery = BBGt(left, right.toString("YYYY-DD-MMTHH:mm:ssZZ"))

    def lt(right: DateTime): SearchQuery = BBLt(left, right.toString("YYYY-DD-MMTHH:mm:ssZZ"))

    def ===(right: String): SearchQuery = BBEQString(left, right)

    def ===(right: Int): SearchQuery = BBEQ(left, right.toString)

    def ===(right: Long): SearchQuery = BBEQ(left, right.toString)

    def ===(right: Boolean): SearchQuery = BBEQ(left, right.toString)

    def !==(right: String): SearchQuery = BB_NEQ_String(left, right)

    def !==(right: Int): SearchQuery = BB_NEQ(left, right.toString)

    def !==(right: Long): SearchQuery = BB_NEQ(left, right.toString)

    def !==(right: Boolean): SearchQuery = BB_NEQ(left, right.toString)

    def ~(right: String): SearchQuery = BB_Contain_String(left, right)

    def !~(right: String): SearchQuery = BB__Not_Contain_String(left, right)
  }

  def BBStateP(right: BBPullRequestState): SearchQuery = BBEQString("state", right.name)

  def BBStateP(data: List[BBPullRequestState]): SearchQuery =
    BBGroup(data.map(BBStateP).reduce(_ or _))

  def BBStateP(right: BBPullRequestState, more: BBPullRequestState*): SearchQuery =
    BBStateP(right :: more.toList)

}


case class Contains(field: String, value: String) extends SearchQuery {
  override def render: String = URLEncoder.encode(s"$field ~ $value", "UTF-8")
}