package org.obl.briscola

import org.obl.ddd._
import org.obl.briscola.competition._
import org.obl.briscola.player._

import scalaz.{ -\/, \/, \/- }

trait CompetitionsService {

  def createCompetition(issuer: PlayerId, players: Set[PlayerId], kind: MatchKind, deadLine: CompetitionStartDeadline): BriscolaError \/ CompetitionState

  def acceptCompetition(pid: PlayerId, cid: CompetitionId): Option[BriscolaError \/ CompetitionState]

  def declineCompetition(pid: PlayerId, cid: CompetitionId, reason: Option[String]): Option[BriscolaError \/ CompetitionState]

  def allCompetitions:Iterable[CompetitionState]
  def competitionById(id:CompetitionId):Option[CompetitionState]
}
trait BaseCompetitionsService extends CompetitionsService {

  protected def competitionRepository: CompetitionRepository

  protected def competitionRunner: (CompetitionState, CompetitionCommand) => BriscolaError \/ (Seq[CompetitionEvent], CompetitionState)

  private def runCommand(st: CompetitionState, cmd: CompetitionCommand): BriscolaError \/ CompetitionState = {
    competitionRunner(st, cmd).map { p =>
      val (_, comp) = p
      comp match {
        case EmptyCompetition => ()
        case comp: OpenCompetition => competitionRepository.put(comp.competition.id, comp)
        case comp: DroppedCompetition => competitionRepository.put(comp.competition.id, comp)
        case comp: FullfilledCompetition => competitionRepository.put(comp.competition.id, comp)
      }
      comp
    }
  }

  def createCompetition(issuer: PlayerId, players: Set[PlayerId], kind: MatchKind, deadLine: CompetitionStartDeadline): BriscolaError \/ CompetitionState =
    runCommand(EmptyCompetition, CreateCompetition(issuer, players, kind, deadLine))

  def acceptCompetition(pid: PlayerId, cid: CompetitionId): Option[BriscolaError \/ CompetitionState] =
    competitionRepository.get(cid).map { cs =>
      runCommand(cs, AcceptCompetition(pid, cid))
    }

  def declineCompetition(pid: PlayerId, cid: CompetitionId, reason: Option[String]): Option[BriscolaError \/ CompetitionState] =
    competitionRepository.get(cid).map { cs =>
      runCommand(cs, DeclineCompetition(pid, cid, reason))
    }

  def allCompetitions:Iterable[CompetitionState] = competitionRepository.all
  def competitionById(id:CompetitionId):Option[CompetitionState] = competitionRepository.get(id)
}