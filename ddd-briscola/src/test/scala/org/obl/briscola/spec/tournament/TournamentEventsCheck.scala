package org.obl.briscola.spec.tournament

import org.scalacheck._
import org.scalacheck.Gen._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.{ forAll, BooleanOperators }
import org.cvogt.scalacheck.GenTree
import org.obl.briscola.player._
import org.obl.briscola.tournament._
import org.obl.briscola.competition._
import org.obl.ddd.Runner
import org.obl.briscola._

object TournamentEventsCheck extends scala.App {

  val avaiablePlayers = 1.to(3).map(id => PlayerId(id))

  implicit val aPlayerId = Arbitrary(Gen.oneOf(avaiablePlayers))

  implicit val aCompetitionStartDeadline = Arbitrary(GenTree.tree[CompetitionStartDeadline])
  
  val aCommand = {
    implicit val aSeed = Gen.oneOf(Seed.values.toSeq)
    val aValidCardNumber = Gen.oneOf(1.to(10).map(_.toByte))

    val aCardGen = for (
      seed <- aSeed;
      num <- aValidCardNumber
    ) yield {
      Card(num, seed)
    }
    implicit val aCard = Arbitrary(aCardGen)
    implicit val aDeck = Arbitrary(Gen.oneOf(Deck.initial, Deck.empty))

    val aGameId = Gen.oneOf( 1.to(10).map( GameId(_) ) )

    implicit val aPlayerScore = Arbitrary(for ( cards <- Gen.listOf(aCardGen) ) yield (Score(cards.toSet):Score))
    
    implicit val anActiveGameState = Arbitrary(for (
      gameId <- aGameId;
      briscolaCard <- aCardGen;
      nextPlayers <- Gen.listOfN(2, GenTree.tree[PlayerState])
    ) yield {
      ActiveGameState(gameId, briscolaCard, Deck.initial, Nil, nextPlayers, None)
    })
    
    implicit val aFinalGameState = Arbitrary(for (
      gameId <- aGameId;
      briscolaCard <- aCardGen;
      nextPlayers <- Gen.listOfN(2, GenTree.tree[PlayerFinalState])
    ) yield {
      FinalGameState(gameId, briscolaCard, nextPlayers, None)
    })
    
    implicit val aDroppedGameState = Arbitrary(for (
      gameId <- aGameId;
      briscolaCard <- aCardGen;
      nextPlayers <- Gen.listOfN(2, GenTree.tree[PlayerState]);
      dropReason <- GenTree.tree[PlayerLeft]
    ) yield {
      DroppedGameState(gameId, briscolaCard, Deck.initial, Nil, nextPlayers, dropReason, None)
    })
  
    val aMatchKind = Gen.oneOf[MatchKind](SingleMatch, NumberOfGamesMatchKind(1))
    val aStartTournament = for (
      players <- Gen.listOf(Gen.oneOf(avaiablePlayers));
      mkind <- aMatchKind
    ) yield {
      StartTournament(players.toSet, mkind)
    }
    
    val aSetTournamentGame = GenTree.tree[SetTournamentGame]
    val aSetGameOutcome = GenTree.tree[SetGameOutcome]
    val aDropTournamentGame = GenTree.tree[DropTournamentGame]

    Gen.oneOf(aStartTournament, aSetTournamentGame, aSetGameOutcome, aDropTournamentGame)
  }

  val commandsList = Gen.listOf(aCommand)

  val decider = new TournamentDecider {

    def playerById(playerId: PlayerId) = Some(Player(playerId, playerId.id.toString, ""))

  }

  val evolver = new TournamentEvolver {
    def nextId: TournamentId = TournamentId(1)
  }

  lazy val runner = Runner.fromCommandSeq(decider, evolver)
  lazy val changesRunner = Runner.changesFromCommandSeq(decider, evolver)

  val runnerShouldNotCrash = forAll(commandsList) { cmds =>
    runner(EmptyTournamentState, cmds).map(v => if (v._1.length > 0) v._2 != EmptyTournamentState else true).getOrElse(true)
  }

  val idNeverChange = forAll(commandsList) { cmds =>
    changesRunner(EmptyTournamentState, cmds).map { sc =>
      sc.forall { sc =>
        TournamentState.id(sc.oldState).zip(TournamentState.id(sc.state)).forall { (i) =>
          i._1 == i._2
        }
      }
    }.getOrElse(true)
  }

  runnerShouldNotCrash.check(_.withMinSuccessfulTests(1500))
  idNeverChange.check(_.withMinSuccessfulTests(1500))

}