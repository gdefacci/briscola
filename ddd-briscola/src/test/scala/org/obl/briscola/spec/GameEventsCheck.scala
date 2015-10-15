package org.obl.briscola
package spec

import org.scalacheck._
import org.scalacheck.Gen._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.{forAll, BooleanOperators}
import org.cvogt.scalacheck.GenTree

import org.obl.briscola.player.PlayerId
import org.obl.briscola.player.Player
import org.obl.ddd.Runner

object GameEventsCheck extends scala.App {

  val avaiablePlayers = 1.to(3).map( id => PlayerId(id) )
  
  implicit val aPlayerId = Arbitrary( Gen.oneOf(avaiablePlayers) )
  implicit val aSeed = Gen.oneOf(Seed.values.toSeq) 
  val aValidCardNumber = Gen.oneOf(1.to(10).map(_.toByte)) 
  
  implicit val aCard1 = Arbitrary( for (seed <- aSeed; 
      num <- aValidCardNumber) yield {
    Card(num, seed)
  })
  
  val aCommand = GenTree.tree[BriscolaCommand]
  
  val commandsList = Gen.listOf(aCommand)

  val decider = new GameDecider {

    def nextId: GameId = GameId(1)
    def playerById(playerId: PlayerId) = Some(Player(playerId, playerId.id.toString, ""))

  }

  val evolver = new GameEvolver {}
  
  lazy val runner = Runner.fromCommandSeq(decider, evolver)
  lazy val changesRunner = Runner.changesFromCommandSeq(decider, evolver)
  
  val runnerShouldNotCrash = forAll(commandsList) { cmds =>
    runner(EmptyGameState, cmds).map(v => if (v._1.length > 0) v._2 != EmptyGameState else true).getOrElse(true) 
  }
  
  val idNeverChange = forAll(commandsList) { cmds =>
    changesRunner(EmptyGameState, cmds).map { sc =>
      sc.forall { sc =>
        GameState.id(sc.oldState).zip(GameState.id(sc.state)).forall { (i) =>
          i._1 == i._2
        }
      }
    }.getOrElse(true)
  }
  
  runnerShouldNotCrash.check(_.withMinSuccessfulTests(1500))
  idNeverChange.check(_.withMinSuccessfulTests(1500))
}