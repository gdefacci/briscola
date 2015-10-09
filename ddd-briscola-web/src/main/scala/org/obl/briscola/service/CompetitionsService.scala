package org.obl.briscola
package service

import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.Observable

import org.obl.ddd._
import org.obl.briscola.competition._
import org.obl.briscola.player._

import scalaz.{ -\/, \/, \/- }

trait CompetitionsService {

  def createCompetition(issuer: PlayerId, players: Set[PlayerId], kind: MatchKind, deadLine: CompetitionStartDeadline): CompetitionError \/ CompetitionState

  def acceptCompetition(pid: PlayerId, cid: CompetitionId): Option[CompetitionError \/ CompetitionState]

  def declineCompetition(pid: PlayerId, cid: CompetitionId, reason: Option[String]): Option[CompetitionError \/ CompetitionState]

  def allCompetitions:Iterable[CompetitionState]
  def competitionById(id:CompetitionId):Option[CompetitionState]
  
  def setCompetionGame(cid: CompetitionId, gid:GameId): Option[CompetitionError \/ CompetitionState] 
  
  def changes:Observable[StateChange[CompetitionState, CompetitionEvent]]
  
  def isFullfilled(comp:CompetitionId):Boolean
}
trait BaseCompetitionsService extends CompetitionsService {

  protected def competitionRepository: CompetitionRepository

  protected def competitionRunner: (CompetitionState, CompetitionCommand) => CompetitionError \/ Seq[StateChange[CompetitionState, CompetitionEvent]]

  private lazy val changesChannel = ReplaySubject[StateChange[CompetitionState, CompetitionEvent]]
  
  private def runCommand(st: CompetitionState, cmd: CompetitionCommand): CompetitionError \/ CompetitionState = {
    competitionRunner(st, cmd).map { cngs =>
      val comp = cngs.last.state
      comp match {
        case EmptyCompetition => ()
        case OpenCompetition(id,_,_,_) => competitionRepository.put(id, comp)
        case DroppedCompetition(id,_,_,_) => competitionRepository.put(id, comp)
        case FullfilledCompetition(id,_,_,_,_) => competitionRepository.put(id, comp)
      }
      cngs.foreach(changesChannel.onNext(_))
      comp
    }
  }
  def isFullfilled(comp:CompetitionId):Boolean = competitionRepository.get(comp) match {
    case Some(FullfilledCompetition(_,_,_,_,_)) => true
    case _ => false 
  }
  
  def changes:Observable[StateChange[CompetitionState, CompetitionEvent]] = changesChannel

  def createCompetition(issuer: PlayerId, players: Set[PlayerId], kind: MatchKind, deadLine: CompetitionStartDeadline): CompetitionError \/ CompetitionState =
    runCommand(EmptyCompetition, CreateCompetition(issuer, players, kind, deadLine))

  def acceptCompetition(pid: PlayerId, cid: CompetitionId): Option[CompetitionError \/ CompetitionState] =
    competitionRepository.get(cid).map { cs =>
      runCommand(cs, AcceptCompetition(pid))
    }

  def declineCompetition(pid: PlayerId, cid: CompetitionId, reason: Option[String]): Option[CompetitionError \/ CompetitionState] =
    competitionRepository.get(cid).map { cs =>
      runCommand(cs, DeclineCompetition(pid, reason))
    }
  
  def setCompetionGame(cid: CompetitionId, gid:GameId): Option[CompetitionError \/ CompetitionState]  =
    competitionRepository.get(cid).map { cs =>
      runCommand(cs, SetCompetitonGame(gid))
    }

  def allCompetitions:Iterable[CompetitionState] = competitionRepository.all
  def competitionById(id:CompetitionId):Option[CompetitionState] = competitionRepository.get(id)
}