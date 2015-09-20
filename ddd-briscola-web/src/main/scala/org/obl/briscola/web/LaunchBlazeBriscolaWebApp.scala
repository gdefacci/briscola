package org.obl.briscola.web

import org.obl.raz._
import org.obl.briscola.Config
import org.http4s.server.blaze.BlazeBuilder

object LaunchBlazeBriscolaWebApp extends App {

  val routes = AppRoutesImpl(AbsolutePath(HTTP("localhost", 8080)))
  val webApp = new BriscolaWebApp(routes, Config.simple.app)

  BlazeBuilder.bindHttp(8080)
    .mountService(webApp.gamesPlan.plan, "/")
    .mountService(webApp.competitionsPlan.plan, "/")
    .mountService(webApp.playersPlan.plan, "/")
    .run
    .awaitShutdown()

}  