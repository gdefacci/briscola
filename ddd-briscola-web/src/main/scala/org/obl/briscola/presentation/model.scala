package org.obl.briscola.presentation

final case class EventAndState[E,S](event:E, state:S)    
final case class Collection[T](members:Iterable[T])
  
trait ADT[E <: Enumeration] {
  def kind:E#Value
}