package com.github.gdefacci.briscola.tournament 

import com.github.gdefacci.briscola.player._
import com.github.gdefacci.briscola.competition.MatchKind
import com.github.gdefacci.briscola.game.{FinalGameState, GameId, ActiveGameState, DropReason, PlayerFinalState}

sealed trait TournamentState

object TournamentState {
  
  def id(s: TournamentState): Option[TournamentId] = s match {
    case EmptyTournamentState => None
    case ActiveTournamentState(id, _, _, _, _) => Some(id)
    case CompletedTournamentState(id, _, _, _, _) => Some(id)
    case DroppedTournamentState(id, _, _, _, _, _) => Some(id)
  }
  
} 

final case class TournamentId(id:Long)

case object EmptyTournamentState extends TournamentState

final case class PlayerTournamentState(id: PlayerId, points:Int, numberOfWonCards:Int)

sealed trait TournamentStateResults {
  
  def finishedGames:Map[GameId, FinalGameState]
  
  lazy val playersTournamentState: Map[PlayerId, PlayerTournamentState] = 
    finishedGames.foldLeft(Map.empty[PlayerId, PlayerTournamentState]) { (mp, e) =>
      e._2.players.foldLeft(mp) { (mp, pl) =>
        mp.get(pl.id) match {
          case None => mp + (pl.id -> PlayerTournamentState(pl.id, pl.points, pl.score.cards.size))
          case Some(ps) => mp + (pl.id -> PlayerTournamentState(pl.id, pl.points+ps.points, pl.score.cards.size+ps.numberOfWonCards))
        }
      }
    } 
  
  lazy val playersTournamentStateSortedByScore: Seq[PlayerTournamentState] =
    playersTournamentState.values.toSeq.sortWith { (a,b) =>
      (a.points > b.points) || (a.numberOfWonCards > b.numberOfWonCards)
    }
  
}

final case class ActiveTournamentState(id:TournamentId, players:GamePlayers, kind:MatchKind, 
    finishedGames:Map[GameId, FinalGameState], currentGames:Map[GameId, ActiveGameState]) extends TournamentState with TournamentStateResults
    
final case class CompletedTournamentState(id:TournamentId, players:GamePlayers, kind:MatchKind, 
    finishedGames:Map[GameId, FinalGameState], currentGames:Map[GameId, ActiveGameState]) extends TournamentState with TournamentStateResults
    
final case class DroppedTournamentState(id:TournamentId, players:GamePlayers, kind:MatchKind, 
    finishedGames:Map[GameId, FinalGameState], currentGames:Map[GameId, ActiveGameState],
    dropReason:DropReason) extends TournamentState with TournamentStateResults