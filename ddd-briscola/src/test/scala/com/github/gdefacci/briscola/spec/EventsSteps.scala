package com.github.gdefacci.briscola.spec

import scalaz.{ -\/, \/, \/- }
import com.github.gdefacci.ddd._

import com.github.gdefacci.bdd._

trait EventsTestState[S, E, Err] {
  def result: Err \/ (Seq[E], S)

}

trait EventsSteps[TS <: EventsTestState[S, E, Err], S, E, Err] extends BDD[TS, scalaz.Id.Id,String] {

  def `events contain`(event: E => Boolean): Expectation = expectation { state =>
    state.result match {
      case -\/(err) => Fail(err.toString)
      case \/-((events, _)) =>
        if (events.exists(event)) Ok else Fail(s"expecting $event inside ${events.mkString}, but was not")
    }
  }

  def `error is`(error: Err => Boolean): Expectation = expectation { state =>
    state.result match {
      case -\/(err) => if (error(err)) Ok else Fail(s"expecting error $error but got error $err")
      case \/-((_, _)) =>
        Fail(s"expecting error $error but no error happened")
    }
  }

  def `the final state`(predicate: S => Boolean): Expectation = expectation { state =>
    state.result match {
      case -\/(err) => Fail(err.toString)
      case \/-((_, newState)) =>
        if (predicate(newState)) Ok else Fail(s"expecting state ${predicate.toString} but got ${newState}")
    }
  }

}  
