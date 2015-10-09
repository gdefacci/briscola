package org.obl.briscola
package service
package impl.simple

import org.obl.briscola._
import org.obl.ddd.Repository

class SimpleGameRepository extends SimpleRepository[GameId, GameState] with GameRepository {
  
  private val counter = new java.util.concurrent.atomic.AtomicLong(0)
  
  def newId = GameId(counter.incrementAndGet)
  def all = valuesMap.values
  

}