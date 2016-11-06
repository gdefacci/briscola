package com.github.gdefacci.briscola.service.competition.impl.simple

import com.github.gdefacci.briscola.competition._
import com.github.gdefacci.briscola.service.competition._

class SimpleCompetitionRepository extends CompetitionRepository {
  
  protected val valuesMap = collection.concurrent.TrieMap.empty[CompetitionId, CompetitionState]
  
  def byId(id:CompetitionId) = valuesMap.get(id)
  
  def store(t:CompetitionState):Unit = {
    CompetitionState.id(t).foreach( id =>
      valuesMap += (id -> t)
    )
  }
  
  def all:Iterable[CompetitionState] = valuesMap.values
}