package org.obl.briscola
package service

import org.obl.ddd.Repository
import org.obl.briscola.player._

trait PlayerRepository extends Repository[PlayerId, Player] {
  def newId:PlayerId
  def containsName(nm:String):Boolean
  def all:Iterable[Player]
  
  def byName(name:String):Option[Player]
  
}
