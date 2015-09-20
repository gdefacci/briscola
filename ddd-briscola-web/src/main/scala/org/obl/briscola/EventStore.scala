package org.obl.briscola

import org.obl.ddd.Event

trait EventStore[E <: Event] {
  def put(event:E):Unit
  
  def events:Iterable[E]
  
}