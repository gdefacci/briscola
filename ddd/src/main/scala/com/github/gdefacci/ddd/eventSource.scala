package com.github.gdefacci.ddd

import scalaz.{-\/, \/, \/-}

trait Decider[S, C, E, Err] {
  
  def apply(s:S, cmd:C):Err \/ Seq[E]
  
}

trait Evolver[S, E] {
  
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

final case class StateChange[S,E](oldState:S, event:E, state:S)
final case class TStateChange[SA,E,SB](oldState:SA, event:E, newState:SB) 


