package org.obl.briscola
package impl.simple

import org.obl.ddd.Event

class SimpleEventStore[E <: Event] extends EventStore[E] {
  
  private val buffer = collection.mutable.Buffer.empty[E]
  
  def put(event:E) = buffer += event
  
  def events:Seq[E] = buffer
  
}