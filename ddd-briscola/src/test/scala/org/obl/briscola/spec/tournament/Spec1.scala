package org.obl.briscola.spec.tournament

import org.obl.ddd.spec.PrintlnReporter
import org.obl.briscola.tournament._
import org.obl.briscola.player._
import org.obl.briscola.competition.SingleMatch
import org.obl.briscola._

object Spec1 extends App with TournamentSpec {

  val reporter = new PrintlnReporter[TournamentState, TournamentEvent, TournamentError]
  
  {
    val playersMin = Set(PlayerId(1))
    val playersMax = 1.to(GameState.MAX_PLAYERS+1).map(id => PlayerId(id)).toSet

    check(
      When(StartTournament(playersMin, SingleMatch)).expect(ErrorIs(TournamentGameError(TooFewPlayers(playersMin, GameState.MIN_PLAYERS))))
    )
    check(
      When(StartTournament(playersMax, SingleMatch)).expect(ErrorIs(TournamentGameError(TooManyPlayers(playersMax, GameState.MAX_PLAYERS))))
    )
  }
  
  {
    val players = 1.to(3).map(id => PlayerId(id)).toSet
    val game = ActiveGameState(GameId(1), Card(7, Seed.bastoni), Deck.empty, Seq.empty, Seq(PlayerState(PlayerId(1), Set.empty, PlayerScore.empty))) 
    val fgame = FinalGameState(GameId(1), Card(7, Seed.bastoni), Nil) 
    
    check(
      When(StartTournament(players, SingleMatch)).expect(
        EventsThat("contains a single TournamentStarted event") {
          case Seq(TournamentStarted(pls1, SingleMatch)) => pls1.map(_.id) == players
          case _ => false
        } and StateThatIs[ActiveTournamentState]("is an empty active tournament") { st =>
          st.currentGames.isEmpty && st.finishedGames.isEmpty && st.kind == SingleMatch && st.players == players
        }    
      ).andThenOnNewState[ActiveTournamentState] { ns =>
        When(SetTournamentGame(game)).expect(
          EventsThat("a new tournament game has been started") {
            case Seq(TournamentGameHasStarted(gm1)) => game == gm1
            case _ => false
          }    
        ).andThenOnNewState[ActiveTournamentState]( ns => 
          When(SetGameOutcome(fgame)).expect(
            EventsThat("a new tournament game has been started") {
              case Seq(TournamentGameHasFinished(gm2)) => fgame == gm2
              case _ => false
            } and
            StateThatIs[CompletedTournamentState]("is a completed tournament")(i => true)
          )
        )
      }
    )
  }

}