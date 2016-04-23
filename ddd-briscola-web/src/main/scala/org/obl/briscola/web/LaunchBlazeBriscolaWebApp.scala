package org.obl.briscola.web

import org.obl.raz._
import org.obl.briscola.service.Config
import org.http4s.server.blaze.BlazeBuilder

object LaunchBlazeBriscolaWebApp extends App {

  val routes = new AppRoutes(new Resources(Authority("localhost", 8080), Path / "app", RoutesServletConfig))
  val webApp = new BriscolaWebApp(routes, Config.simple.app)

  BlazeBuilder.bindHttp(8080)
    .withWebSockets(true)
    .mountService(webApp.gamesPlan.plan, "/")
    .mountService(webApp.competitionsPlan.plan, "/")
    .mountService(webApp.playersPlan.plan, "/")
    .mountService(webApp.siteMapPlan.plan, "/")
    .mountService(Static.plan, "/")
    .run
    .awaitShutdown()

}  