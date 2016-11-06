package com.github.gdefacci.briscola.spec.tournament

import com.github.gdefacci.ddd._
import com.github.gdefacci.bdd._
import testkit.Predicates._

import com.github.gdefacci.briscola.player._
import com.github.gdefacci.briscola.tournament._
import com.github.gdefacci.briscola.competition.SingleMatch
import com.github.gdefacci.briscola.game.TooFewPlayers
import com.github.gdefacci.briscola.game._

object TournamentFeatures extends Features with TournamentSteps {

  val `cant start a tournament with just one player` = {

    val player1 = Player(PlayerId(1), "1", "1")

    scenario(
      `Given that logged players are`(player1)
        When `issue the command`(StartTournament(Players(Set(player1.id)), SingleMatch))
        Then `error is`(`equal to`(TournamentGameError(TooFewPlayers(Set(player1.id), GameState.MIN_PLAYERS)))))
  }

  val `cant start a tournament with too many players` = {

    val player1 = Player(PlayerId(1), "1", "1")
    val otherPlayers = 2.to(GameState.MAX_PLAYERS + 1).map(idx => Player(PlayerId(idx), s"$idx", s"$idx")).toSet
    val allPlayers = otherPlayers + player1

    scenario(
      `Given that logged players are`(allPlayers.toSeq: _*)
        When `issue the command`(StartTournament(Players(allPlayers.map(_.id)), SingleMatch))
        Then `error is`(`equal to`(TournamentGameError(TooManyPlayers(allPlayers.map(_.id), GameState.MAX_PLAYERS)))))
  }
  
  val `a tournament can be started` = {

    val players = 1.to(3).map(idx => Player(PlayerId(idx), s"$idx", s"$idx")).toSet
    val game = ActiveGameState(GameId(1), Card(7, Seed.bastoni), Deck.empty, Seq.empty, Seq(PlayerState(PlayerId(1), Set.empty, Score.empty)), None)
    val fgame = FinalGameState(GameId(1), Card(7, Seed.bastoni), Nil, None)

    def `the proper tournament started event`: Predicate[TournamentEvent] = predicate {
      case TournamentStarted(pls1, SingleMatch) => 
        
        GamePlayers.getPlayers(pls1) == players.map(_.id)
      case _ => false
    }

    scenario(
      `Given that logged players are`(players.toSeq: _*)
        When `issue the command`(StartTournament(Players(players.map(_.id)), SingleMatch))
        Then `events contain`(`the proper tournament started event`)
        And `the final state`(`is an active tournament`))

  }
  
  val `can bind a game to a tournament` = {

    val players = 1.to(3).map(idx => Player(PlayerId(idx), s"$idx", s"$idx")).toSet
    val game = ActiveGameState(GameId(1), Card(7, Seed.bastoni), Deck.empty, Seq.empty, Seq(PlayerState(PlayerId(1), Set.empty, Score.empty)), None)
    val fgame = FinalGameState(GameId(1), Card(7, Seed.bastoni), Nil, None)

    def `the proper tournament game started event`: Predicate[TournamentEvent] = predicate {
      case TournamentGameHasStarted(gm1) => game == gm1
      case _ => false
    }

    scenario(
      `Given that logged players are`(players.toSeq: _*)
        When `issue the command`(StartTournament(Players(players.map(_.id)), SingleMatch))
        And `issue the command`(SetTournamentGame(game))
        Then `events contain`(`the proper tournament game started event`)
        And `the final state`(`is an active tournament`))

  }

  val `a single match tournament can be completed` = {

    val players = 1.to(3).map(idx => Player(PlayerId(idx), s"$idx", s"$idx")).toSet
    val game = ActiveGameState(GameId(1), Card(7, Seed.bastoni), Deck.empty, Seq.empty, Seq(PlayerState(PlayerId(1), Set.empty, Score.empty)), None)
    val fgame = FinalGameState(GameId(1), Card(7, Seed.bastoni), Nil, None)

    def `the proper finished tournament event`: Predicate[TournamentEvent] = predicate {
      case TournamentGameHasFinished(gm2) => gm2 == fgame
      case _ => false
    }

    scenario(
      `Given that logged players are`(players.toSeq: _*)
        When `issue the command`(StartTournament(Players(players.map(_.id)), SingleMatch))
        And `issue the command`(SetTournamentGame(game))
        And `issue the command`(SetGameOutcome(fgame))
        Then `events contain`(`the proper finished tournament event`)
        And `the final state`(`is a completed tournament`))

  }

  lazy val features = new Feature("Tournament features",
    `cant start a tournament with just one player`,
    `cant start a tournament with too many players`,
    `a tournament can be started`,
    `can bind a game to a tournament`,
    `a single match tournament can be completed`) :: Nil
}