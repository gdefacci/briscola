package com.github.gdefacci.briscola
package competition

import player._
import com.github.gdefacci.ddd._
import scalaz.{ -\/, \/, \/- }
import java.time.LocalDateTime

import com.github.gdefacci.briscola.game._

trait CompetitionDecider extends Decider[CompetitionState, CompetitionCommand, CompetitionEvent, CompetitionError] {

  def nextId: CompetitionId
  def playerById(id: PlayerId): Option[Player]

  def apply(s: CompetitionState, cmd: CompetitionCommand): CompetitionError \/ Seq[CompetitionEvent] = {
    (s -> cmd) match {
      case (EmptyCompetition, CreateCompetition(issuer, gmPlayers, kind, deadLine)) =>
        val gamePlayers: GamePlayers = gmPlayers match {
          case Players(players) => Players(players + issuer)
          case gpls => gpls
        }
        GamePlayersValidator.withValidPlayersAndTeams(gamePlayers, playerById(_)) { (players, teams) =>
          val issr = players.find(_.id == issuer).get
          Seq(CreatedCompetition(nextId, issr, Competition(gamePlayers, kind, deadLine)))
        } leftMap(CompetioBriscolaError(_))
        
      case (comp: OpenCompetition, AcceptCompetition(playerId)) =>
        val players = GamePlayers.getPlayers(comp.competition.players)
        if (comp.acceptingPlayers.contains(playerId)) {
          -\/(CompetitionAlreadyAccepted)
        } else if (players.contains(playerId)) {
          \/-(Seq(CompetitionAccepted(playerId)))
        } else {
          -\/(CompetioBriscolaError(InvalidPlayer(playerId)))
        }
      case (comp: OpenCompetition, DeclineCompetition(playerId, reason)) =>
        val players = GamePlayers.getPlayers(comp.competition.players)
        if (comp.decliningPlayers.contains(playerId)) {
          -\/(CompetitionAlreadyDeclined)
        } else if (players.contains(playerId)) {
          \/-(Seq(CompetitionDeclined(playerId, reason)))
        } else {
          -\/(CompetioBriscolaError(InvalidPlayer(playerId)))
        }
      case (EmptyCompetition, _) => -\/(CompetitionNotStarted)
      case (comp: OpenCompetition, cmd: CreateCompetition) => -\/(CompetitionAlreadyStarted)
      case (c: DroppedCompetition, _) => -\/(CompetitionDropped)
      case (c: FullfilledCompetition, _) => -\/(CompetitionAlreadyFinished)
    }
  }

}

trait CompetitionEvolver extends Evolver[CompetitionState, CompetitionEvent] {

  def isFullfilled(comp: Competition, players: Set[PlayerId]) = {
    val compPlayers = GamePlayers.getPlayers(comp.players)
    comp.deadline match {
      case CompetitionStartDeadline.AllPlayers => compPlayers == players
      case CompetitionStartDeadline.OnPlayerCount(n) => compPlayers.size == n
    }
  }

  def isDropped(comp: Competition, decliningPlayers: Set[PlayerId]) = {
    comp.deadline match {
      case CompetitionStartDeadline.AllPlayers => decliningPlayers.nonEmpty
      case CompetitionStartDeadline.OnPlayerCount(n) => (GamePlayers.getPlayers(comp.players).size - decliningPlayers.size) < n
    }
  }

  def apply(s: CompetitionState, event: CompetitionEvent): CompetitionState = {
    (s -> event) match {
      case (EmptyCompetition, CreatedCompetition(id, issuer, comp)) =>
        OpenCompetition(id, comp, Set(issuer.id), Set.empty)

      case (OpenCompetition(id, competition, acceptingPlayers, decliningPlayers), CompetitionAccepted(pid)) =>
        if (isFullfilled(competition, acceptingPlayers + pid))
          FullfilledCompetition(id, competition, acceptingPlayers + pid, decliningPlayers - pid)
        else
          OpenCompetition(id, competition, acceptingPlayers + pid, decliningPlayers - pid)

      case (OpenCompetition(id, competition, acceptingPlayers, decliningPlayers), CompetitionDeclined(pid, rsn)) =>
        if (isDropped(competition, decliningPlayers + pid))
          DroppedCompetition(id, competition, acceptingPlayers - pid, decliningPlayers + pid)
        else
          OpenCompetition(id, competition, acceptingPlayers - pid, decliningPlayers + pid)

      case _ => {
        throw new RuntimeException(s"forbidden condition state:${s} event:${event}")

      }
    }
  }
}