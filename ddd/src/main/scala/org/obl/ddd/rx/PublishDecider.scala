package org.obl.ddd
package rx

import _root_.rx.lang.scala.Subject
import _root_.rx.lang.scala.Observable
import scalaz.\/

class PublishDecider[S <: State, C <: Command, E <: Event, Err <: DomainError](decider:Decider[S,C,E,Err]) extends Decider[S,C,E,Err] {
  
  private val channel = Subject[Decision[S,C,E,Err]]()
  
  def apply(s:S, cmd:C):Err \/ Seq[E] = {
    val r = decider(s,cmd)
    channel.onNext(Decision(s,cmd,r))
    r
  }
  
  lazy val decisions:Observable[Decision[S,C,E,Err]] = channel
  
}

case class Decision[S,C,E,Err](state:S, command:C, result:Err \/ Seq[E])

class PublishEvolver[S <: State, E <: Event](evolver:Evolver[S,E]) extends Evolver[S,E] {
  
  private val channel = Subject[StateChange[S,E]]()
  
  def apply(s:S, event:E):S = {
    val r = evolver(s,event)
    channel.onNext(StateChange(s,event,r))
    r
  }
  
  lazy val changes:Observable[StateChange[S,E]] = channel
  
}

case class StateChange[S,E](oldState:S, event:E, state:S)