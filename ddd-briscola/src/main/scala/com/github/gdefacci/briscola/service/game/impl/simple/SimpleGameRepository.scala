package com.github.gdefacci.briscola.service.game.impl.simple

import com.github.gdefacci.briscola.game._
import com.github.gdefacci.briscola.service.game._

class SimpleGameRepository extends GameRepository {
  
  protected val valuesMap = collection.concurrent.TrieMap.empty[GameId, GameState]
  
  def store(t:GameState):Unit = {
    GameState.id(t).foreach( id =>
      valuesMap += (id -> t)
    )
  }
  
  def allGames: Iterable[GameState] = valuesMap.values   
  def gameById(id: GameId): Option[GameState] = valuesMap.get(id) 	


}