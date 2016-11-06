package com.github.gdefacci.briscola.presentation.sitemap

import com.github.gdefacci.briscola.presentation
import com.github.gdefacci.briscola.presentation.player.PlayerRoutes
import com.github.gdefacci.briscola.presentation.RoutesServletConfig

object SiteMapModule {
  
  def siteMap(playerRoutes:PlayerRoutes) =
      presentation.sitemap.SiteMap(playerRoutes.Players.path, playerRoutes.PlayerLogin.path)

  def plans(siteMap:presentation.sitemap.SiteMap, routesServletConfig:RoutesServletConfig, siteMapRoutes:SiteMapRoutes) = 
    new SiteMapPlan(routesServletConfig.siteMap, siteMapRoutes, siteMap) 

}