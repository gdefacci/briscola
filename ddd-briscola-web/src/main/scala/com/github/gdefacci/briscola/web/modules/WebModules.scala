package com.github.gdefacci.briscola.web.modules

import com.github.gdefacci.di.runtime.ModulesContainer
import com.github.gdefacci.briscola.presentation.player.PlayersModule
import com.github.gdefacci.briscola.presentation.game.GameModule
import com.github.gdefacci.briscola.presentation.competition.CompetitionModule
import com.github.gdefacci.briscola.presentation.sitemap.SiteMapModule

object WebModules extends ModulesContainer {

  val routesModule = RoutesModule
  val playersModule = PlayersModule
  val gameModule = GameModule
  val competitionModule = CompetitionModule
  val siteMapModule = SiteMapModule
  val jsonEncodeModule = JsonEncodeModule
  val webModule = WebModule

}
