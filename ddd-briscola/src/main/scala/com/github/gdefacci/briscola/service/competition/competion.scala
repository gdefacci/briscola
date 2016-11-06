package com.github.gdefacci.briscola.service.competition

import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.Observable
import com.github.gdefacci.ddd._
import com.github.gdefacci.briscola.competition._
import com.github.gdefacci.briscola.player._
import scalaz.{ -\/, \/, \/- }
import com.github.gdefacci.ddd.rx.ObservableCommandRunner

trait CompetitionsService {

  def createCompetition(issuer: PlayerId, players: GamePlayers, kind: MatchKind, deadLine: CompetitionStartDeadline): CompetitionError \/ CompetitionState

  def acceptCompetition(pid: PlayerId, cid: CompetitionId): CompetitionError \/ Option[CompetitionState]

  def declineCompetition(pid: PlayerId, cid: CompetitionId, reason: Option[String]): CompetitionError \/ Option[CompetitionState]

  def allCompetitions:Iterable[CompetitionState]
  def competitionById(id:CompetitionId):Option[CompetitionState]
  
  def changes:Observable[StateChange[CompetitionState, CompetitionEvent]]
  
  def isFullfilled(comp:CompetitionId):Boolean
  
  def competitionsFullfilled:Observable[TStateChange[OpenCompetition, CompetitionEvent, FullfilledCompetition]] 
}

trait CompetitionRepository {
  
  def all:Iterable[CompetitionState]
  def byId(id:CompetitionId):Option[CompetitionState]
  
  def store(id:CompetitionState):Unit
}

class CompetitionsServiceImpl(
    runner: ObservableCommandRunner[CompetitionState, CompetitionCommand, CompetitionEvent, CompetitionError],
    repository: CompetitionRepository) extends CompetitionsService {
  
  lazy val changes = runner.changes

  def aggregateId(comp:CompetitionState) = CompetitionState.id(comp)
  
  def isFullfilled(comp:CompetitionId):Boolean = repository.byId(comp) match {
    case Some(FullfilledCompetition(_,_,_,_)) => true
    case _ => false 
  }
  
  def createCompetition(issuer: PlayerId, players: GamePlayers, kind: MatchKind, deadLine: CompetitionStartDeadline): CompetitionError \/ CompetitionState =
    runner.run(EmptyCompetition, CreateCompetition(issuer, players, kind, deadLine))

  def acceptCompetition(pid: PlayerId, cid: CompetitionId): CompetitionError \/ Option[CompetitionState] = {
    import scalaz._
    import Scalaz._
    
    repository.byId(cid).map { cs =>
      runner.run(cs, AcceptCompetition(pid))
    }.sequenceU
  }

  def declineCompetition(pid: PlayerId, cid: CompetitionId, reason: Option[String]): CompetitionError \/ Option[CompetitionState] = {
    import scalaz._
    import Scalaz._

    repository.byId(cid).map { cs =>
      runner.run(cs, DeclineCompetition(pid, reason))
    }.sequenceU
  }
  
  def allCompetitions:Iterable[CompetitionState] = repository.all
  def competitionById(id:CompetitionId):Option[CompetitionState] = repository.byId(id)
  
  lazy val competitionsFullfilled:Observable[TStateChange[OpenCompetition, CompetitionEvent, FullfilledCompetition]] = changes.collect {
    case StateChange(sa @ OpenCompetition(_,_,_,_), e, sb @ FullfilledCompetition(_,_,_,_)) => TStateChange(sa,e,sb) 
  }
}