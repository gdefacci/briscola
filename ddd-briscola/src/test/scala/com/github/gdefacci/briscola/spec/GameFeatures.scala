package com.github.gdefacci.briscola
package spec

import scalaz.{ -\/, \/, \/- }

import com.github.gdefacci.briscola.player._
import com.github.gdefacci.briscola.game._
import com.github.gdefacci.bdd._
import testkit.Predicates._

object GameFeatures extends GameSteps with Features {

  lazy val `a game cant be started with just one player` = {

    val player1 = Player(PlayerId(1), "player1", "player1")

    scenario(
      `Given an empty game with logged players`(player1)
        When `issue the command`(StartGame(Players(Set(player1.id))))
        Then `error is`(`equal to`(TooFewPlayers(Set(player1.id), GameState.MIN_PLAYERS))))
  }

  lazy val `a game cant be started with too many players` = {

    val players = 1.to(GameState.MAX_PLAYERS + 1).map(idx => Player(PlayerId(idx), s"player$idx", s"player$idx")).toSeq
    val player1 = players.head

    scenario(
      `Given an empty game with logged players`(players: _*)
        When `issue the command`(StartGame(Players(Set(player1.id))))
        Then `error is`(`equal to`(TooFewPlayers(Set(player1.id), GameState.MIN_PLAYERS))))
  }

  lazy val `a game can be started` = {
    val player1 = Player(PlayerId(1), "player1", "player1")
    val player2 = Player(PlayerId(2), "player2", "player2")

    lazy val `a GameStarted event`: Predicate[BriscolaEvent] = predicate {
      case GameStarted(_) => true
      case _ => false
    }

    lazy val `no moves has been made`: Predicate[GameState] = predicate {
      case ActiveGameState(_, _, _, moves, _, _) => moves.isEmpty
      case _ => false
    }

    scenario(
      `Given an empty game with logged players`(player1, player2)
        When `issue the command`(StartGame(Players(Set(player1.id, player2.id))))
        Then `events contain`(`a GameStarted event`)
        And `the final state`(
          `is an ActiveGameState` and
            `no moves has been made` and
            `has a deck with a total of cards`(34) and
            `include player`(player1) and
            `include player`(player2)))
  }

  lazy val `a game hand can be concluded` = {
    val player1 = Player(PlayerId(1), "player1", "player1")
    val player2 = Player(PlayerId(2), "player2", "player2")

    lazy val `a GameStarted event`: Predicate[BriscolaEvent] = predicate {
      case GameStarted(_) => true
      case _ => false
    }

    scenario(
      `Given an empty game with logged players`(player1, player2)
        And `issue the command`(StartGame(Players(Set(player1.id, player2.id))))
        And `the current player play a random card`
        And `the current player play a random card`
        Then `the final state`(`is an ActiveGameState` and `has a deck with a total of cards`(32)))
  }

  lazy val `a game can be concluded` = {
    val player1 = Player(PlayerId(1), "player1", "player1")
    val player2 = Player(PlayerId(2), "player2", "player2")

    scenario(
      `Given an empty game with logged players`(player1, player2)
        And `issue the command`(StartGame(Players(Set(player1.id, player2.id))))
        And `repeat step until state`(`the current player play a random card`, `is a FinalGameState`)
        Then `the final state`(`is a FinalGameState`)
        And `the sum off all players points is`(120))
  }

  lazy val `a game cant be started more tha once` = {
    val player1 = Player(PlayerId(1), "player1", "player1")
    val player2 = Player(PlayerId(2), "player2", "player2")

    scenario(
      `Given an empty game with logged players`(player1, player2)
        When `issue the command`(StartGame(Players(Set(player1.id, player2.id))))
        And `issue the command`(StartGame(Players(Set(player1.id, player2.id))))
        Then `error is`(`equal to`(GameAlreadyStarted)))
  }

  lazy val `every team must have the same number of players` = {
    val player1 = Player(PlayerId(1), "player1", "player1")
    val players2 = 2.to(3).map(idx => Player(PlayerId(idx), s"player$idx", s"player$idx")).toSet

    val teamInfoA = TeamInfo("TeamA")
    val teamInfoB = TeamInfo("TeamB")

    val teamPlayers = TeamPlayers(players2.map(p => TeamPlayer(p.id, teamInfoB)) + TeamPlayer(player1.id, teamInfoA))

    scenario(
      `Given an empty game with logged players`((player1 +: players2.toSeq): _*)
        When `issue the command`(StartGame(teamPlayers))
        Then `error is`(`equal to`(TeamsMustHaveSameNumberOfPlayers(teamPlayers.teams))))
  }

  lazy val `a game cant be started with just one team` = {

    val player1 = Player(PlayerId(1), "player1", "player1")

    scenario(
      `Given an empty game with logged players`(player1)
        When `issue the command`(StartGame(Players(Set(player1.id))))
        Then `error is`(`equal to`(TooFewPlayers(Set(player1.id), GameState.MIN_PLAYERS))))
  }

  lazy val `a game cant be started with too many teams` = {

    val teamInfoA = TeamInfo("TeamA")
    val teamInfoB = TeamInfo("TeamB")
    val teamInfoC = TeamInfo("TeamC")
    val teamInfoD = TeamInfo("TeamD")
    val teamInfoE = TeamInfo("TeamE")

    val (players, tplayers) = 1.to(5).map { idx =>
      val player = Player(PlayerId(idx), s"player$idx", s"player$idx")
      val teamInfo = (idx % 5) match {
        case 1 => teamInfoA
        case 2 => teamInfoB
        case 3 => teamInfoC
        case 4 => teamInfoD
        case _ => teamInfoE
      }
      player -> TeamPlayer(player.id, teamInfo)
    }.toSet.unzip

    val teamPlayers = TeamPlayers(tplayers)

    scenario(
      `Given an empty game with logged players`(players.toSeq: _*)
        When `issue the command`(StartGame(teamPlayers))
        Then `error is`(`equal to`(TooManyTeams(teamPlayers.teams, GameState.MAX_TEAMS_NUMBER))))
  }
  
  lazy val `a team must have more tha one player` = {

    val teamInfoA = TeamInfo("TeamA")
    val teamInfoB = TeamInfo("TeamB")

    val (players, tplayers) = 1.to(2).map { idx =>
      val player = Player(PlayerId(idx), s"player$idx", s"player$idx")
      val teamInfo = (idx % 2) match {
        case 1 => teamInfoA
        case _ => teamInfoB
      }
      player -> TeamPlayer(player.id, teamInfo)
    }.toSet.unzip

    val teamPlayers = TeamPlayers(tplayers)

    scenario(
      `Given an empty game with logged players`(players.toSeq: _*)
        When `issue the command`(StartGame(teamPlayers))
        Then `error is`(`equal to`(TooFewPlayersPerTeam(teamPlayers.teams, GameState.TEAM_MIN_PLAYERS_NUMBER))))
  }

  lazy val `team points are countend in the proper way` = {
    val players1 = 1.to(2).map(idx => Player(PlayerId(idx), s"player$idx", s"player$idx")).toSet
    val players2 = 2.to(32).map(idx => Player(PlayerId(idx), s"player$idx", s"player$idx")).toSet

    val pid1 = players1.head.id
    val pid2 = players1.last.id
    val pid3 = players2.head.id
    val pid4 = players2.last.id

    val gid = GameId(1)

    val teamA = Team("TeamA", players1.map(_.id))
    val teams = Teams(Set(
      teamA,
      Team("TeamB", players2.map(_.id))))

    val state = ActiveGameState(gid, Card(1, Seed.coppe), Deck.empty, Seq(
      Move(PlayerState(pid1, Set.empty, Score.empty), Card(2, Seed.coppe)),
      Move(PlayerState(pid2, Set.empty, Score.empty), Card(3, Seed.spade)),
      Move(PlayerState(pid3, Set.empty, Score.empty), Card(2, Seed.denari))),
      Seq(PlayerState(pid4, Set(Card(7, Seed.spade)), Score.empty)), Some(teams))

    scenario(`Given an empty game with logged players`((players1 ++ players2).toSeq: _*)
      And `the initial game state is`(state)
      When `issue the command`(PlayCard(pid4, Card(7, Seed.spade)))
      Then `the final state`(`is a FinalGameState`)
      And `the winner team is`(teamA))

  }

  lazy val `a player can belong only one team` = {
    val players = 1.to(2).map(idx => Player(PlayerId(idx), s"player$idx", s"player$idx")).toSet

    val teamInfoA = TeamInfo("TeamA")
    val teamInfoB = TeamInfo("TeamB")

    val teamPlayers = TeamPlayers(players.map(p => TeamPlayer(p.id, teamInfoB)) ++ players.map(p => TeamPlayer(p.id, teamInfoA)))

    scenario(
      `Given an empty game with logged players`(players.toSeq: _*)
        And `issue the command`(StartGame(teamPlayers))
        Then `error is`(
          `equal to`[BriscolaError](PlayerCanHaveOnlyOneTeam(PlayerId(3), Set(teamInfoA, teamInfoB))) or
            `equal to`[BriscolaError](PlayerCanHaveOnlyOneTeam(PlayerId(4), Set(teamInfoA, teamInfoB)))
            ))
  }

  lazy val `a team game can be concluded` = {
    val players1 = 1.to(2).map(idx => Player(PlayerId(idx), s"player$idx", s"player$idx")).toSet
    val players2 = 3.to(4).map(idx => Player(PlayerId(idx), s"player$idx", s"player$idx")).toSet

    val teamInfoA = TeamInfo("TeamA")
    val teamInfoB = TeamInfo("TeamB")

    val teamPlayers = TeamPlayers(players1.map(p => TeamPlayer(p.id, teamInfoB)) ++ players2.map(p => TeamPlayer(p.id, teamInfoA)))

    scenario(
      `Given an empty game with logged players`((players1 ++ players2).toSeq: _*)
        And `issue the command`(StartGame(teamPlayers))
        And `repeat step until state`(`the current player play a random card`, `is a FinalGameState`)
        Then `the final state`(`is a FinalGameState`)
        And `the sum off all players points is`(120))
  }

  lazy val features = List(
    new Feature(
      "Game features",
      `a game cant be started with just one player`,
      `a game cant be started with too many players`,
      `a game cant be started more tha once`,
      `a game can be started`,
      `a game hand can be concluded`,
      `a game can be concluded`),
    new Feature(
      "Game Team features",
      `every team must have the same number of players`,
      `a game cant be started with just one team`,
      `a game cant be started with too many teams`,
      `a team must have more tha one player`,
      `team points are countend in the proper way`,
      `a team game can be concluded`))

}