package org.obl.briscola

import org.obl.briscola.competition._
import org.obl.ddd.Repository

trait CompetitionRepository extends Repository[CompetitionId, CompetitionState] {
  
  def newId:CompetitionId
  def all:Iterable[CompetitionState]
  
}
