package com.github.gdefacci.briscola.service.impl.simple

import com.github.gdefacci.briscola.service

class repositories {
  
  val competitionRepository = new com.github.gdefacci.briscola.service.competition.impl.simple.SimpleCompetitionRepository
  val gameRepository = new com.github.gdefacci.briscola.service.game.impl.simple.SimpleGameRepository
	val playerRepository = new service.player.impl.simple.SimplePlayerRepository
	val tournamentRepository = new service.tournament.impl.simple.SimpleTournamentRepository
  
}