package org.obl.briscola
package impl.simple

import org.obl.ddd._
import org.obl.briscola.player._

class SimplePlayerRepository extends SimpleRepository[PlayerId, Player]() with PlayerRepository {
  
  private val counter = new java.util.concurrent.atomic.AtomicLong(0)
  
  def newId = PlayerId(counter.incrementAndGet)

  def containsName(nm:String) = valuesMap.values.exists(_.name==nm)
  
  def all:Iterable[Player] = valuesMap.values
  
}
