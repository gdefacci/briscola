package org.obl.briscola
package service
package impl.simple

import org.obl.briscola.competition._
import org.obl.ddd.Repository

class SimpleCompetitionRepository extends SimpleRepository[CompetitionId, CompetitionState] with CompetitionRepository {
  
  private val counter = new java.util.concurrent.atomic.AtomicLong(0)
  
  def newId = CompetitionId(counter.incrementAndGet)

  def all:Iterable[CompetitionState] = valuesMap.values
}