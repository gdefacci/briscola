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
  
  def fromCommandSeq[S <: State, C <: Command, E <: Event, Err <: DomainError](decider:Decider[S,C,E,Err], evolver:Evolver[S,E]):(S,Seq[C]) => Err \/ (Seq[E], S) = {
    val runner = apply[S,C,E,Err](decider, evolver)
    (state, commands) => {
      val z:Err \/ (Seq[E], S) = \/-(Nil, state)
      commands.foldLeft(z) { (acc, cmd) =>
        acc.flatMap { p =>
          val (events, state) = p
          runner(state, cmd).map( e1 => (events ++ e1._1) -> e1._2)
        }
      }      
    }
  }
  
  def changesFromCommandSeq[S <: State, C <: Command, E <: Event, Err <: DomainError](decider:Decider[S,C,E,Err], evolver:Evolver[S,E]):(S,Seq[C]) => Err \/ Seq[StateChange[S,E]] = {
    val runner = changes[S,C,E,Err](decider, evolver)
    (state, commands) => {
      val z:Err \/ (Seq[StateChange[S,E]], S) = \/-(Nil, state)
      commands.foldLeft(z) { (acc, cmd) =>
        acc.flatMap { p =>
          val (changes, state) = p
          runner(state, cmd).map( sc => (changes ++ sc) -> sc.last.state )
        }
      }.map(_._1)      
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

final case class StateChange[S,E](oldState:S, event:E, state:S)