package io.morgaroth.bitbucketclient.models

import org.joda.time.DateTime

case class BBTag(
                  name: String,
                  links: BBLinksCommits,
                  tagger: Option[BBUser],
                  date: Option[DateTime],
                  message: Option[String],
                  `type`: String,
                  target: BBTagTarget
                )

case class BBTagTarget(
                        hash: String,
                        repository: BBRepository,
                        author: BBAuthor,
                        parents: Vector[BBCommit],
                        links: BBTagTargetLinks,
                        date: DateTime,
                        message: String,
                        `type`: String,

                      )

case class BBTagTargetLinks(
                             self: BBLink,
                             html: BBLink,
                             comments: BBLink,
                             diff: BBLink,
                             approve: BBLink,
                             statuses: BBLink,
                           )
