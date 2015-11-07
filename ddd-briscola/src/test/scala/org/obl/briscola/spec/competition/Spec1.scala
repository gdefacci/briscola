package org.obl.briscola.spec.competition

import org.obl.ddd._
import org.obl.ddd.spec._
import org.obl.briscola._
import org.obl.briscola.player._
import org.obl.briscola.competition._

object Spec1 extends App with CompetitionSpec {

  val reporter = new PrintlnReporter[CompetitionState, CompetitionEvent, CompetitionError]

  {

    val player1 = PlayerId(1)
    val players = Set(player1)

    check(
      When(CreateCompetition(player1, Players(Set.empty), SingleMatch, CompetitionStartDeadline.AllPlayers)) expect
        ErrorIs(CompetioBriscolaError(TooFewPlayers(players, GameState.MIN_PLAYERS))))

    check(
      When(CreateCompetition(player1, Players(players), SingleMatch, CompetitionStartDeadline.AllPlayers)) expect
        ErrorIs(CompetioBriscolaError(TooFewPlayers(players, GameState.MIN_PLAYERS))))

  }

  {

    val player1 = PlayerId(1)
    val players = 2.to(GameState.MAX_PLAYERS + 1).map(PlayerId(_)).toSet

    check(
      When(CreateCompetition(player1, Players(players), SingleMatch, CompetitionStartDeadline.AllPlayers)) expect
        ErrorIs(CompetioBriscolaError(TooManyPlayers(players + player1, GameState.MAX_PLAYERS))))

  }

  {

    val players = 1.to(3).map(PlayerId(_)).toSet
    val issuer = players.head

    check(
      (When(CreateCompetition(issuer, Players(players), SingleMatch, CompetitionStartDeadline.AllPlayers)) expect
        EventsThat("include only CreatedCompetition") {
          case Seq(CreatedCompetition(_, issuer1, Competition(players1, SingleMatch, CompetitionStartDeadline.AllPlayers))) =>
            issuer == issuer1.id && players == GamePlayers.getPlayers(players1)
          case x => false
        }).andThenOnNewState[OpenCompetition] { comp =>
          (When(AcceptCompetition(issuer))
            expect EventsAre(CompetitionAccepted(issuer))).andThenOnNewState[OpenCompetition] { comp =>
              (When(AcceptCompetition(PlayerId(2)))
                expect EventsAre(CompetitionAccepted(PlayerId(2)))).andThenOnNewState[OpenCompetition] { comp =>
                  When(AcceptCompetition(PlayerId(3))) expect (
                    EventsAre(CompetitionAccepted(PlayerId(3))) and
                    StateThatIs[FullfilledCompetition]("competition is fullfilled")(i => true))
                }
            }
        })

  }

  {
    val players = 1.to(3).map(PlayerId(_)).toSet
    val issuer = players.head

    check(
      (When(CreateCompetition(issuer, Players(players), SingleMatch, CompetitionStartDeadline.AllPlayers)) expect
        EventsThat("include only CreatedCompetition") {
          case Seq(CreatedCompetition(_, issuer1, Competition(players1, SingleMatch, CompetitionStartDeadline.AllPlayers))) =>
            issuer == issuer1.id && players == GamePlayers.getPlayers(players1)
          case x => false
        }).andThenOnNewState[OpenCompetition] { comp =>
          (When(AcceptCompetition(issuer))
            expect EventsAre(CompetitionAccepted(issuer))).andThenOnNewState[OpenCompetition] { comp =>
              (When(DeclineCompetition(PlayerId(2), None))
                expect EventsAre(CompetitionDeclined(PlayerId(2), None))).andThenOnNewState[DroppedCompetition] { comp =>
                  When(DeclineCompetition(PlayerId(3), None)) expect (
                    ErrorIs(CompetitionDropped))
                }
            }
        })

  }

}