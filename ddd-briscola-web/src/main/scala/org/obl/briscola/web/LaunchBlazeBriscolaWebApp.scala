package org.obl.briscola.web

import org.obl.raz._
import org.obl.briscola.service.Config
import org.http4s.server.blaze.BlazeBuilder
import org.obl.briscola.web.util.Plan

object LaunchBlazeBriscolaWebApp extends App {

  val routes = new AppRoutes(new Resources(Authority("localhost", 8080), Path / "app", RoutesServletConfig))
  val webApp = new BriscolaWebApp(routes, Config.simple.app)

  def mountServices(plans: Seq[Plan]): BlazeBuilder => BlazeBuilder = { builder =>
    plans.foldLeft(builder) { (builde, plan) =>
      builder.mountService(plan.plan, "/")
    }
  }
  
  val config = mountServices(webApp.plans)

  config( BlazeBuilder.bindHttp(8080).withWebSockets(true).mountService(Static.plan, "/") )
    .run
    .awaitShutdown()

}  