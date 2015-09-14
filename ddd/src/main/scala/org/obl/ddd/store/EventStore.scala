package org.obl.ddd
package store

trait EventStore[E <: Event] {

  def put(event:E):Unit
  def events:Iterable[E]
  
}