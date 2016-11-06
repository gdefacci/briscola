package com.github.gdefacci.briscola.web.modules

import com.github.gdefacci.briscola.service.GameApp

class GameLayerModule(gameApp:GameApp) {
  
  val services = gameApp.services
  val playerService = services.player
  val gameService = services.game
  val competitionService = services.competition
  
}