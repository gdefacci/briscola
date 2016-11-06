package com.github.gdefacci.briscola.presentation

import org.obl.raz.{Path, Authority}
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.web.util.{ServletPlan, WebSocketChannel, WebSocketEndPoint, WebSocketConfig}
import com.github.gdefacci.briscola.presentation.player.PlayerWebSocketRoutes

class BriscolaWebApp(val plans: Seq[ServletPlan], val channel: WebSocketChannel[PlayerId])

class BriscolaWebAppConfig(
    val webApp:BriscolaWebApp, 
    val playerWebSocketRoutes:PlayerWebSocketRoutes, 
    val host:Authority, 
    val contextPath:Path) {

  class ConfiguredPlayerWebSocketEndPoint extends WebSocketEndPoint(playerWebSocketRoutes.PlayerById, WebSocketConfig.fromObservableFactory(webApp.channel) )

}
