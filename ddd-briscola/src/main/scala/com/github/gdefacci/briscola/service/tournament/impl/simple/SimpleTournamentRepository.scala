package com.github.gdefacci.briscola.service.tournament.impl.simple

import com.github.gdefacci.briscola.tournament._
import com.github.gdefacci.briscola.service.tournament._

class SimpleTournamentRepository extends TournamentRepository {
  
  protected val valuesMap = collection.concurrent.TrieMap.empty[TournamentId, TournamentState]
  
  def byId(id:TournamentId) = valuesMap.get(id)
  
  def store(t:TournamentState):Unit = {
    TournamentState.id(t).foreach( id =>
      valuesMap += (id -> t)
    )
  }
  
  def all = valuesMap.values
  

}