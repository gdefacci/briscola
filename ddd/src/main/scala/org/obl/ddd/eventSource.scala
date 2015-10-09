package org.obl.ddd

import scalaz.{-\/, \/, \/-}

trait Command

trait Event

trait State

trait DomainError

trait Decider[S <: State, C <: Command, E <: Event, Err <: DomainError] {
  
  def apply(s:S, cmd:C):Err \/ Seq[E]
  
}

trait Evolver[S <: State, E <: Event] {
  
  def apply(s:S, event:E):S
  
  def apply(initialState:S, events:Seq[E]):S = 
    events.foldLeft(initialState)( apply )
 
  def changes(initialState:S, events:Seq[E]):Seq[StateChange[S,E]] = {
    val z:(S, Seq[StateChange[S,E]]) = initialState -> Nil
    val r = events.foldLeft(z) { (acc,event) =>
      val (prevState, res) = acc
      val newState = apply(prevState, event)
      newState -> (res :+ StateChange(prevState, event, newState))
    }
    r._2
  }
}

object Runner {
  
  def apply[S <: State, C <: Command, E <: Event, Err <: DomainError](decider:Decider[S,C,E,Err], evolver:Evolver[S,E]):(S,C) => Err \/ (Seq[E], S) = { (state, cmd) =>
    decider(state, cmd).map { evs =>
      evs -> evolver(state, evs)
    }
  }
  
  def changes[S <: State, C <: Command, E <: Event, Err <: DomainError](decider:Decider[S,C,E,Err], evolver:Evolver[S,E]):(S,C) => Err \/ Seq[StateChange[S,E]] = { (state, cmd) =>
    decider(state, cmd).map { evs =>
      evolver.changes(state, evs)
    }
  }
  
}

trait Repository[Id, T] {
  
  def get(id:Id):Option[T]
  def put(id:Id, v:T):Option[T]
  
}

case class StateChange[S,E](oldState:S, event:E, state:S)