package org.obl.ddd
package spec

import scalaz.{ -\/, \/, \/- }

sealed trait Description {
  def +(d1: Description): Description
  def text:String
}

final case class DescriptionImpl(text: String) extends Description {
  def +(d1: Description) = d1 match {
    case d1 @ DescriptionImpl(_) => DescriptionSeq(Seq(this, d1))
    case DescriptionSeq(dss) => DescriptionSeq(this +: dss)
  }
}

final case class DescriptionSeq(descriptions: Seq[Description]) extends Description {
  def +(d1: Description) = d1 match {
    case d1 @ DescriptionImpl(_) => DescriptionSeq(descriptions :+ d1)
    case DescriptionSeq(dss) => DescriptionSeq(descriptions ++ dss)
  }
  def text = descriptions.map(_.text).mkString("\n")
}

sealed trait SuccessfullExpectation {
  def prerequisiteDescription:Description
  def expectationDescription:Description
  
  def description:Description

  override def toString = description.text
} 

sealed trait FailedExpectation[S <: State, E <: Event, Err <: DomainError] {
  def currentState: Err \/ (Seq[E], S)
  def prerequisiteDescription:Description 
  
  def expectationDescription:Description
  
  def description:Description

  override def toString = description.text

}

final case class CheckResult[S <: State, E <: Event, Err <: DomainError](currentState: Err \/ (Seq[E], S), results:Seq[FailedExpectation[S,E,Err] \/ SuccessfullExpectation], sub:Seq[CheckResult[S,E,Err]])

class PrintlnReporter[S <: State, E <: Event, Err <: DomainError] extends (CheckResult[S,E,Err] => Unit) {
  
  def apply(r:CheckResult[S,E,Err]) = {
    apply("", r, "")
  }
  
  private def apply(indent:String, d:Description):String = d match {
    case DescriptionImpl(txt) => s"${indent}$txt"
    case DescriptionSeq(descs) => descs.map(di => apply(indent, di)).mkString("\n")
  }
  
  private def apply(indent:String, r:CheckResult[S,E,Err], prefix:String):Unit = {
    println(
      r.results.map {
        case \/-(exp) => s"${indent}${prefix}Success:\n${apply(indent, exp.description)}"
        case -\/(exp) => s"${indent}${prefix}!!!!!! Error:\n${apply(indent, exp.description)}"
      }.mkString("\n")
    )
    r.sub.foreach(apply(indent+"  ",_,"And after "))
  }
}

trait Spec[S <: State, C <: Command, E <: Event, Err <: DomainError] {

  case class DomainSpec(
      initialState: S,
      eventsAfterInitialState: Seq[E],
      commands: Seq[C],
      expectations: Seq[Expectation]) {

    def run(decider: Decider[S, C, E, Err], evolver: Evolver[S, E]): Err \/ (Seq[E], S) = {
      val si = evolver(initialState, eventsAfterInitialState)
      val z: Err \/ (Seq[E], S) = \/-(Nil -> si)
      lazy val runner = Runner(decider, evolver)
      commands.foldLeft(z) { (acc, cmd) =>
        acc.fold(err => -\/(err), { p =>
          val (evnts, st) = p
          runner(st,cmd).map { p =>
            val (events, newState) = p
            (evnts ++ events) -> newState
          }
        })
      }
    }

  }

  sealed trait Assertion {

    def prerequiste: Prerequisite
    def expectation: Expectation
    def next: Seq[S => Assertion]

    def andThen(after: S => Assertion): Assertion
    
    def andThenOnNewState[S1 <: S](after: S1 => Assertion)(implicit classTag:reflect.ClassTag[S1]) = {
      andThen { s =>
        s match {
          case s1:S1 => after(s1)
          case _ => Assertion(new PrerequisiteImpl(DescriptionImpl(s"state must be of type ${classTag.runtimeClass.getName}"), i => i), UnsatisfiedPrequisite, Nil )
        }
      }
    }
  }
  
  object Expect {
    def apply(expectation:StateExpectation):S => Assertion = { s =>
      OnState(s).expect(expectation)
    }
  }

  object Assertion {
    private class AssertionImpl(val prerequiste: Prerequisite, val expectation: Expectation, val next: Seq[S => Assertion]) extends Assertion {
      def andThen(after: S => Assertion): AssertionImpl =
        new AssertionImpl(prerequiste, expectation, next :+ after)
    }
    def apply(prerequiste: Prerequisite, expectation: Expectation, next: Seq[S => Assertion]): Assertion =
      new AssertionImpl(prerequiste, expectation, next)
  }

  sealed trait Prerequisite {
    def description: Description
    def apply(t: DomainSpec): DomainSpec
    def and(pr1: Prerequisite): Prerequisite
    def expect(exp: Expectation): Assertion
  }

  sealed trait Expectation {
    def description: Description
    def isFullfilled(r: Err \/ (Seq[E], S)): Boolean
    def apply(spec: DomainSpec): DomainSpec 
    def and(exp: Expectation): Expectation
  }

  sealed class PrerequisiteImpl(val description: Description, f: DomainSpec => DomainSpec) extends Prerequisite {
    def apply(t: DomainSpec): DomainSpec = f(t)
    def and(pr1: Prerequisite): Prerequisite = new PrerequisiteImpl(description + pr1.description, f.andThen(pr1.apply))
    def expect(exp: Expectation) = Assertion(this, exp, Nil)
  }
  
  case class OnState(state: S) extends PrerequisiteImpl(DescriptionImpl(s"With initial State($state)"), _.copy(initialState = state) ) 
  case class Given(events: E*) extends PrerequisiteImpl(DescriptionImpl(s"given events ${events.mkString(", ")}"), spec => spec.copy(eventsAfterInitialState = spec.eventsAfterInitialState ++ events) )
  case class When(commands: C*) extends PrerequisiteImpl(DescriptionImpl(s"with commands ${commands.mkString(", ")}"), spec => spec.copy(commands = spec.commands ++ commands) )

  private def andPredicate(a: Expectation, b: Expectation): (Err \/ (Seq[E], S)) => Boolean = r => a.isFullfilled(r) && b.isFullfilled(r)

  sealed abstract class BaseExpectation(val description: Description, predicate: (Err \/ (Seq[E], S)) => Boolean) extends Expectation {
    def and(pr1: Expectation): Expectation = new ExpectationImpl(description + pr1.description, (apply _).andThen(pr1.apply), andPredicate(this, pr1))
    def isFullfilled(r: Err \/ (Seq[E], S)): Boolean = predicate(r)
  }

  sealed class ExpectationImpl(description: Description, f: DomainSpec => DomainSpec, predicate: (Err \/ (Seq[E], S)) => Boolean) extends BaseExpectation(description, predicate) {
    def apply(t: DomainSpec): DomainSpec = f(t)
  }

  sealed abstract class LeafExpectationImpl(description: Description, predicate: (Err \/ (Seq[E], S)) => Boolean) extends BaseExpectation(description, predicate) {
    def apply(t: DomainSpec): DomainSpec = t.copy(expectations = t.expectations :+ this)
  }

  sealed trait StateExpectation extends Expectation {
    def and(exp: StateExpectation): StateExpectation = {
      new ExpectationImpl(description + exp.description, (apply _).andThen(exp.apply), andPredicate(this, exp)) with StateExpectation
    }
  }

  sealed trait EventsExpectation extends Expectation {
    def and(exp: EventsExpectation): EventsExpectation = {
      new ExpectationImpl(description + exp.description, (apply _).andThen(exp.apply), andPredicate(this, exp)) with EventsExpectation
    }
  }

  sealed trait ErrorExpectation extends Expectation {
    def and(exp: ErrorExpectation): ErrorExpectation = {
      new ExpectationImpl(description + exp.description, (apply _).andThen(exp.apply), andPredicate(this, exp)) with ErrorExpectation
    }
  }

  case class EventsAre(events: E*) extends LeafExpectationImpl(DescriptionImpl(s"events ${events.mkString(", ")}"), _.map { case (evs, _) => events == evs } getOrElse false) with EventsExpectation
  case class EventsThat(val textDescription: String)(predicate: Seq[E] => Boolean) extends LeafExpectationImpl(DescriptionImpl(s"events that $textDescription"), _.map { case (evs, _) => predicate(evs) } getOrElse false) with EventsExpectation

  case class ErrorIs(error: Err) extends LeafExpectationImpl(DescriptionImpl(s"error $error"), _.fold(err => err == error, v => false)) with ErrorExpectation
  case class ErrorThat(textDescription: String)(predicate: Err => Boolean) extends LeafExpectationImpl(DescriptionImpl(s"error that $textDescription"), _.fold(predicate, v => false)) with ErrorExpectation
  case object UnsatisfiedPrequisite extends LeafExpectationImpl(DescriptionImpl("unsatisifed prerequisite"), p => false) with ErrorExpectation

  case class StateIs(state: S) extends LeafExpectationImpl(DescriptionImpl(s"state $state"), _.map { case (_, s) => s == state } getOrElse false) with StateExpectation
  case class StateThat(textDescription: String)(predicate: S => Boolean) extends LeafExpectationImpl(DescriptionImpl(s"state that $textDescription"), _.map { case (_, s) => predicate(s) } getOrElse false) with StateExpectation
  case class StateThatIs[S1 <: S](textDescription: String)(predicate: S1 => Boolean)(implicit classTag:reflect.ClassTag[S1]) extends LeafExpectationImpl(
      DescriptionImpl(s"state that $textDescription"), 
      _.map { case (_, s) => s match {
        case s1:S1 => predicate(s1)
        case _ => false
      } } getOrElse false) with StateExpectation

  private final case class SuccessfullExpectationImpl(prerequisiteDescription:Description, expectation: Expectation) extends SuccessfullExpectation {
    lazy val expectationDescription = expectation.description
    def toString(indent:String) = s"$indent${prerequisiteDescription.text}\nexpect ${expectationDescription.text}"
    
    lazy val description = DescriptionSeq(Seq(prerequisiteDescription.text, s"expect ${expectationDescription.text}").map(DescriptionImpl(_)))
  } 
  
  private final case class FailedExpectationImpl(currentState: Err \/ (Seq[E], S), prerequisiteDescription:Description, expectation: Expectation) extends FailedExpectation[S,E,Err] {
    lazy val expectationDescription = expectation.description
    
    def subject = currentState match {
      case \/-((evs, s)) => expectation match {
        case _: EventsExpectation => s"events ${evs.mkString(", ")}"
        case _: StateExpectation => s"state $s"
        case _ => s"result: state: $s events: ${evs.mkString(", ")}"
      }
      case -\/(err) => s"error $err"
    }
    
    lazy val description = DescriptionSeq(  Seq(prerequisiteDescription.text, s"expect ${expectationDescription.text} but got $subject").map(DescriptionImpl(_)))
  }
  
  def check(desc:String, spec: Assertion):Unit = {
    check(Some(desc), spec)
  }
  def check(spec: Assertion):Unit = {
    check(None, spec)
  }
  private def check(desc:Option[String], spec: Assertion):Unit = {
    reporter(performCheck(desc.map(DescriptionImpl(_)), spec, initialState))
  }
  
  private def performCheck(description:Option[Description], spec: Assertion, initialState: S): CheckResult[S,E,Err] = {
    val domainSpec = (spec.prerequiste.apply _).andThen(spec.expectation.apply)(DomainSpec(initialState, Seq.empty, Seq.empty, Seq.empty))
    val res = domainSpec.run(decider, evolver)
    val prereqDescription = spec.prerequiste.description
    val results:Seq[FailedExpectation[S,E,Err] \/ SuccessfullExpectation] = 
      domainSpec.expectations.map(exp => if (!exp.isFullfilled(res)) -\/(FailedExpectationImpl(res, prereqDescription, exp)) else \/-(SuccessfullExpectationImpl(description.getOrElse(prereqDescription),  exp)))
    
    val initialCheckRes = CheckResult(res, results, Nil)
    lazy val failure = initialCheckRes.copy(results = initialCheckRes.results :+ -\/(FailedExpectationImpl(initialCheckRes.currentState, DescriptionImpl("On success"), UnsatisfiedPrequisite)))
    
    spec.next.foldLeft(initialCheckRes) { (checkResult, nextAssert) =>
      checkResult.currentState match {
        case -\/(err) => failure //checkResult.copy(results = checkResult.results :+ -\/(FailedExpectationImpl(checkResult.currentState, DescriptionImpl("On success"), UnsatisfiedPrequisite)))
        case \/-((evs, newState)) => {
          val newCheckResult = performCheck(None, nextAssert(newState), newState)
          checkResult.copy(sub = checkResult.sub :+ newCheckResult)
        }
      }
    }
  }

  def initialState: S
  def decider: Decider[S, C, E, Err]
  def evolver: Evolver[S, E]

  def reporter:CheckResult[S,E,Err] => Unit

}