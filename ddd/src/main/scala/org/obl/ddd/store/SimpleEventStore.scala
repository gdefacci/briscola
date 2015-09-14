package org.obl.ddd
package store

class SimpleEventStore[E <: Event] extends EventStore[E] {
  
  private val buffer = collection.mutable.Buffer.empty[E]
  
  def put(event:E) = this.synchronized( buffer += event )
  
  def events:Iterable[E] = buffer
  
}