package org.obl.briscola.spec.competition

import org.scalacheck._
import org.scalacheck.Gen._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.{forAll, BooleanOperators}
import org.cvogt.scalacheck.GenTree

import org.obl.briscola.player._
import org.obl.briscola.competition._
import org.obl.ddd.Runner

object CompetitionEventsCheck extends scala.App {

  val avaiablePlayers = 1.to(3).map( id => PlayerId(id) )
  
  implicit val aPlayerId = Arbitrary( Gen.oneOf(avaiablePlayers) )

  implicit val aCompetitionStartDeadline = Arbitrary( GenTree.tree[CompetitionStartDeadline] )
  implicit val aMatchKind = Arbitrary( GenTree.tree[MatchKind] )
  val aCommand = GenTree.tree[CompetitionCommand]
  
  val commandsList = Gen.listOf(aCommand)

  val decider = new CompetitionDecider {

    def nextId: CompetitionId = CompetitionId(1)
    def playerById(playerId: PlayerId) = Some(Player(playerId, playerId.id.toString, ""))

  }

  val evolver = new CompetitionEvolver {}

  lazy val runner = Runner.fromCommandSeq(decider, evolver)
  lazy val changesRunner = Runner.changesFromCommandSeq(decider, evolver)
  
  val runnerShouldNotCrash = forAll(commandsList) { cmds =>
    runner(EmptyCompetition, cmds).map(v => if (v._1.length > 0) v._2 != EmptyCompetition else true).getOrElse(true) 
  }
  
  val idNeverChange = forAll(commandsList) { cmds =>
    changesRunner(EmptyCompetition, cmds).map { sc =>
      sc.forall { sc =>
        CompetitionState.id(sc.oldState).zip(CompetitionState.id(sc.state)).forall { (i) =>
          i._1 == i._2
        }
      }
    }.getOrElse(true)
  }

  runnerShouldNotCrash.check(_.withMinSuccessfulTests(1500))
  idNeverChange.check(_.withMinSuccessfulTests(1500))
  
}