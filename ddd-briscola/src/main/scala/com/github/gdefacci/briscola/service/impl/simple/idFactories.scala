package com.github.gdefacci.briscola.service
package impl.simple

import java.util.concurrent.atomic.AtomicLong
import com.github.gdefacci.briscola.game.GameId
import com.github.gdefacci.briscola.competition.CompetitionId
import com.github.gdefacci.briscola.tournament.TournamentId
import com.github.gdefacci.briscola.player.PlayerId

object idFactories {
  
  private def incrementedLong[T](f:Long => T):IdFactory[T] = {
    val counter = new AtomicLong(0)
    IdFactory(() => f(counter.incrementAndGet()))
  }
  
  lazy val game:IdFactory[GameId] = incrementedLong(GameId)
  
  lazy val player:IdFactory[PlayerId] = incrementedLong(PlayerId)
  
  lazy val competition:IdFactory[CompetitionId] = incrementedLong(CompetitionId)
  lazy val tournament:IdFactory[TournamentId] = incrementedLong(TournamentId)
  
}

