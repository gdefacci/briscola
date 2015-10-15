package org.obl.briscola.service

import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject
import org.obl.ddd._
import scalaz.{ -\/, \/, \/- }

trait BaseAggregateService[Id, S <: State, C <: Command, E <: Event, Err <: DomainError] {

  protected def repository: Repository[Id, S]

  protected def runner: (S, C) => Err \/ Seq[StateChange[S, E]]

  private lazy val changesChannel = ReplaySubject[StateChange[S, E]]

  protected def runCommand(st: S, cmd: C): Err \/ S = {
    runner(st, cmd).map { chngs =>
      val st = chngs.last.state
      aggregateId(st).foreach { id =>
        repository.put(id, st)
      }
      chngs.foreach(changesChannel.onNext(_))
      st
    }
  }

  lazy val changes: Observable[StateChange[S, E]] = changesChannel

  protected def aggregateId(s: S): Option[Id]

}