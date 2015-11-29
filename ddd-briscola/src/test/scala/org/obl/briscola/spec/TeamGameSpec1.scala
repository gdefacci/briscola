package org.obl.briscola
package spec

import org.obl.ddd._
import org.obl.ddd.spec._
import org.obl.briscola.player._

object TeamGameSpec1 extends App with BriscolaSpec {

  val reporter = new PrintlnReporter[GameState, BriscolaEvent, BriscolaError]

  {
    val teamA = Set(1,2).map(PlayerId(_))

    val teamB = Set(3,4,5).map(PlayerId(_))

    val teamPlayers = TeamPlayers(
      teamA.map(pid => TeamPlayer(pid, "TeamA")) ++
      teamB.map(pid => TeamPlayer(pid, "TeamB")),
      Set(TeamInfo("TeamA"), TeamInfo("TeamB")))

    val teams = Teams(Seq(
      Team("TeamA", teamA), Team("TeamB", teamB)))

    check(
      When(StartGame(teamPlayers)) expect
        ErrorIs(TeamsMustHaveSameNumberOfPlayers(teams)))
  }

  {
    val pid1 = PlayerId(1)
    val pid2 = PlayerId(2)
    val teamA = Set(pid1, pid2)

    val pid3 = PlayerId(3)
    val pid4 = PlayerId(4)
    val teamB = Set(pid3, pid4)

    val gid = GameId(1)

    val teams = Teams(Seq(
      Team("TeamA", teamA), Team("TeamB", teamB)))

    val state = ActiveGameState(gid, Card(1, Seed.coppe), Deck.empty, Seq(
      Move(PlayerState(pid1, Set.empty, Score.empty), Card(2, Seed.coppe)),
      Move(PlayerState(pid2, Set.empty, Score.empty), Card(3, Seed.spade)),
      Move(PlayerState(pid3, Set.empty, Score.empty), Card(2, Seed.denari))),
      Seq(PlayerState(pid4, Set(Card(7, Seed.spade)), Score.empty)), Some(teams))

    check(
      OnState(state) and When(PlayCard(pid4, Card(7, Seed.spade))) expect (
        EventsAre(CardPlayed(pid4, Card(7, Seed.spade))) and
        StateThatIs[FinalGameState]("TeamA is the winner")(s => {
          val winnner = s.winnerTeam.get.team.name
          winnner == "TeamA"
        })))
  }

}  
  
