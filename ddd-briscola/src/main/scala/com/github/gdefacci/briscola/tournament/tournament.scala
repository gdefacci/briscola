package com.github.gdefacci.briscola.tournament

import scalaz.{ -\/, \/, \/- }
import com.github.gdefacci.ddd.Decider
import com.github.gdefacci.briscola.game.{PlayerLeft, GamePlayersValidator, BriscolaError}
import com.github.gdefacci.ddd.Evolver
import com.github.gdefacci.briscola.competition.SingleMatch
import com.github.gdefacci.briscola.competition.NumberOfGamesMatchKind
import com.github.gdefacci.briscola.competition.TargetPointsMatchKind
import com.github.gdefacci.briscola.player._

trait TournamentDecider extends Decider[TournamentState, TournamentCommand, TournamentEvent, TournamentError] {

  def playerById(pid: PlayerId): Option[Player]

  def apply(s: TournamentState, cmd: TournamentCommand): TournamentError \/ Seq[TournamentEvent] = {
    s match {
      case EmptyTournamentState => cmd match {
        case StartTournament(gamePlayers, matchKind) =>
          GamePlayersValidator.withValidPlayersAndTeams(gamePlayers, playerById(_)) { (players, teams) =>
            Seq(TournamentStarted(gamePlayers, matchKind))
          }.leftMap(TournamentGameError(_))
        case _ => -\/(TournamentNotStarted)
      }
      case ActiveTournamentState(_, _, _, _, currents) => cmd match {
        case StartTournament(_, _) => -\/(TournamentAlreadyStarted)
        case SetTournamentGame(game) => \/-(Seq(TournamentGameHasStarted(game)))
        case SetGameOutcome(game) if !currents.contains(game.id) => -\/(GameDoesNotBelongTournament(game))
        case SetGameOutcome(game) => \/-(Seq(TournamentGameHasFinished(game)))
        case DropTournamentGame(game) => \/-(Seq(TournamentHasBeenDropped(game.dropReason)))
      }
      case CompletedTournamentState(_, _, _, _, _) => -\/(TournamentAlreadyCompleted)
      case DroppedTournamentState(_, _, _, _, _, _) => -\/(TournamentAlreadyDropped)
    }
  }

}

trait TournamentEvolver extends Evolver[TournamentState, TournamentEvent] {

  def nextId: TournamentId

  private def isCompleted(t: ActiveTournamentState) = {
    t.kind match {
      case SingleMatch => t.finishedGames.size > 0
      case NumberOfGamesMatchKind(n) => t.finishedGames.size >= n
      case TargetPointsMatchKind(points) => t.playersTournamentStateSortedByScore.head.points >= points
    }
  }

  def apply(s: TournamentState, event: TournamentEvent): TournamentState = {
    lazy val invalidStateEvent = new RuntimeException(s"invalid state event combination\nstate:$s\nevent:$event")
    s match {
      case EmptyTournamentState => event match {
        case TournamentStarted(players, matchKind) => ActiveTournamentState(nextId, players, matchKind, Map.empty, Map.empty)
        case _ => throw invalidStateEvent
      }
      case ActiveTournamentState(id, players, kind, finished, currents) => event match {
        case TournamentGameHasStarted(game) =>
          ActiveTournamentState(id, players, kind, finished, currents + (game.id -> game))
        case TournamentHasBeenDropped(dropReason) =>
          DroppedTournamentState(id, players, kind, finished, currents, dropReason)
        case TournamentGameHasFinished(game) =>
          val ns = ActiveTournamentState(id, players, kind, finished + (game.id -> game), currents)
          if (isCompleted(ns)) CompletedTournamentState(id, players, kind, ns.finishedGames, ns.currentGames)
          else ns
        case _ => throw invalidStateEvent
      }
      case _ => throw invalidStateEvent
    }
  }
}
