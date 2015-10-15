package org.obl.briscola
package service

import org.obl.briscola.tournament._
import org.obl.ddd.Repository

trait TournamentRepository extends Repository[TournamentId, TournamentState] {
  
  def newId:TournamentId
  def all:Iterable[TournamentState]
  
}
