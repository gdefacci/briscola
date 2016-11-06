package com.github.gdefacci.briscola.presentation.player

import com.github.gdefacci.briscola.web.util.PresentationAdapter
import com.github.gdefacci.briscola.{player => model}
import com.github.gdefacci.briscola.presentation.competition.CompetitionRoutes

class PlayerPresentationAdapter(routes: PlayerRoutes,
  playerWebSocketRoutes: PlayerWebSocketRoutes,
  competitionRoutes: CompetitionRoutes) {
  

  implicit lazy val playerAdapter = PresentationAdapter((pl: model.Player) =>
    Player(routes.PlayerById.encode(pl.id), pl.name,
      playerWebSocketRoutes.PlayerById.encode(pl.id),
      competitionRoutes.CreateCompetition.encode(pl.id)))
  
  implicit lazy val playerEventAdapter = PresentationAdapter[model.PlayerEvent, PlayerEvent]( (pe: model.PlayerEvent) => pe match {
    case model.PlayerLogOn(pid) => PlayerLogOn(routes.PlayerById.encode(pid))
    case model.PlayerLogOff(pid) => PlayerLogOff(routes.PlayerById.encode(pid))
  })
}

//object PlayerPresentationAdapter {
//  def apply(playerRoutes: PlayerRoutes, playerWebSocketRoutes: PlayerWebSocketRoutes, competitionRoutes: CompetitionRoutes) =
//    new PlayerPresentationAdapter(playerRoutes, playerWebSocketRoutes, competitionRoutes)
//    
//}