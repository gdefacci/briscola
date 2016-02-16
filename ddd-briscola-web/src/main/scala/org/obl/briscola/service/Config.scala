package org.obl.briscola
package service

import org.obl.ddd.Event

trait Config {
  def app:BriscolaApp
}

object Config {
  
  def createSimpleApp = {
    import impl.simple._
    
    new BriscolaAppImpl(
        new SimpleEventStore[Event](),
        new SimpleGameRepository(),
        new SimplePlayerRepository(),
        new SimpleCompetitionRepository(),
        new SimpleTournamentRepository()
    )
  }
  
  lazy val simple = new Config {
    
    import impl.simple._
    
    lazy val app = createSimpleApp
  }
  
}