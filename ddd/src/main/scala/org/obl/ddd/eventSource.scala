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
  
}

object Runner {
  
  def apply[S <: State, C <: Command, E <: Event, Err <: DomainError](decider:Decider[S,C,E,Err], evolver:Evolver[S,E]):(S,C) => Err \/ (Seq[E], S) = { (state, cmd) =>
    decider(state, cmd).map { evs =>
      evs -> evolver(state, evs)
    }
  }
  
}

trait Repository[Id, T] {
  
  def get(id:Id):Option[T]
  def put(id:Id, v:T):Option[T]
  
}