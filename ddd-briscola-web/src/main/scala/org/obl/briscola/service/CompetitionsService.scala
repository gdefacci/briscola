package org.obl.briscola
package service

import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.Observable
import org.obl.ddd._
import org.obl.briscola.competition._
import org.obl.briscola.player._
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola.web.util.TStateChange

trait CompetitionsService {

  def createCompetition(issuer: PlayerId, players: GamePlayers, kind: MatchKind, deadLine: CompetitionStartDeadline): CompetitionError \/ CompetitionState

  def acceptCompetition(pid: PlayerId, cid: CompetitionId): Option[CompetitionError \/ CompetitionState]

  def declineCompetition(pid: PlayerId, cid: CompetitionId, reason: Option[String]): Option[CompetitionError \/ CompetitionState]

  def allCompetitions:Iterable[CompetitionState]
  def competitionById(id:CompetitionId):Option[CompetitionState]
  
  def changes:Observable[StateChange[CompetitionState, CompetitionEvent]]
  
  def isFullfilled(comp:CompetitionId):Boolean
  
  def competitionsFullfilled:Observable[TStateChange[OpenCompetition, CompetitionEvent, FullfilledCompetition]] 
}
trait BaseCompetitionsService extends BaseAggregateService[CompetitionId, CompetitionState, CompetitionCommand, CompetitionEvent, CompetitionError] with CompetitionsService {

  protected def repository: CompetitionRepository

  def aggregateId(comp:CompetitionState) = CompetitionState.id(comp)
  
  def isFullfilled(comp:CompetitionId):Boolean = repository.get(comp) match {
    case Some(FullfilledCompetition(_,_,_,_)) => true
    case _ => false 
  }
  
  def createCompetition(issuer: PlayerId, players: GamePlayers, kind: MatchKind, deadLine: CompetitionStartDeadline): CompetitionError \/ CompetitionState =
    runCommand(EmptyCompetition, CreateCompetition(issuer, players, kind, deadLine))

  def acceptCompetition(pid: PlayerId, cid: CompetitionId): Option[CompetitionError \/ CompetitionState] =
    repository.get(cid).map { cs =>
      runCommand(cs, AcceptCompetition(pid))
    }

  def declineCompetition(pid: PlayerId, cid: CompetitionId, reason: Option[String]): Option[CompetitionError \/ CompetitionState] =
    repository.get(cid).map { cs =>
      runCommand(cs, DeclineCompetition(pid, reason))
    }
  
  def allCompetitions:Iterable[CompetitionState] = repository.all
  def competitionById(id:CompetitionId):Option[CompetitionState] = repository.get(id)
  
  lazy val competitionsFullfilled:Observable[TStateChange[OpenCompetition, CompetitionEvent, FullfilledCompetition]] = changes.collect {
    case StateChange(sa @ OpenCompetition(_,_,_,_), e, sb @ FullfilledCompetition(_,_,_,_)) => TStateChange(sa,e,sb) 
  }
}