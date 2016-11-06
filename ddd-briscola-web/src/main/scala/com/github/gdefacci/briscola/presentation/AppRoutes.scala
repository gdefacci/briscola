package com.github.gdefacci.briscola.presentation

import competition.CompetitionRoutes
import game.GameRoutes
import player.PlayerRoutes
import player.PlayerWebSocketRoutes
import sitemap.SiteMapRoutes

case class AppRoutes(playerRoutes:PlayerRoutes,
    playerWebSocketRoutes:PlayerWebSocketRoutes,
    gameRoutes:GameRoutes,
    competitionRoutes:CompetitionRoutes,
    siteMapRoutes:SiteMapRoutes) 