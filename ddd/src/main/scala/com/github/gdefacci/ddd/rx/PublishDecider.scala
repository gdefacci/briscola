package com.github.gdefacci.ddd.rx

import rx.lang.scala.Observable
import rx.lang.scala.Subject
import scalaz.{ -\/, \/, \/- }
import com.github.gdefacci.ddd.{Evolver, StateChange, Decider, Runner}
import rx.lang.scala.subjects.ReplaySubject

class PublishEvolver[S, E](evolver:Evolver[S,E]) extends Evolver[S,E] {
  
  private val channel = ReplaySubject[StateChange[S,E]]()
  
  def apply(s:S, event:E):S = {
    val r = evolver(s,event)
    channel.onNext(StateChange(s,event,r))
    r
  }
  
  lazy val changes:Observable[StateChange[S,E]] = channel
  
}

trait ObservableCommandRunner[S, C, E, Err] extends ((S, C) => Err \/ Seq[StateChange[S, E]]) {
  def changes:Observable[StateChange[S,E]]
  
  def run(s:S, c:C):Err \/ S = apply(s,c).map(_.last.state)
}

object ObservableCommandRunner {
  
  def apply[S, C, E, Err](decider: Decider[S, C, E, Err], evolver: PublishEvolver[S, E]) = new ObservableCommandRunner[S,C,E,Err] {
    val runner = Runner.changes(decider, evolver) 
    
    def apply(s:S, c:C) = runner(s,c)
    
    lazy val changes:Observable[StateChange[S,E]] = evolver.changes
  }
  
  
}