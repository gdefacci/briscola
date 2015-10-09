package org.obl.briscola
package service

import org.obl.briscola._
import org.obl.ddd.Repository

trait GameRepository extends Repository[GameId, GameState] {
  
  def newId:GameId
  def all:Iterable[GameState]
  
}
