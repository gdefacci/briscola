package org.obl.briscola
package service
package impl.simple

import org.obl.briscola.tournament._
import org.obl.ddd.Repository

class SimpleTournamentRepository extends SimpleRepository[TournamentId, TournamentState] with TournamentRepository {
  
  private val counter = new java.util.concurrent.atomic.AtomicLong(0)
  
  def newId = TournamentId(counter.incrementAndGet)
  def all = valuesMap.values
  

}