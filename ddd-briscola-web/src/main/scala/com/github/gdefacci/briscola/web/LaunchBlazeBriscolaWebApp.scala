package com.github.gdefacci.briscola.web

import org.obl.raz._
import org.http4s.server.blaze.BlazeBuilder
import com.github.gdefacci.briscola.web.util.Plan
import com.github.gdefacci.briscola.service.GameApp
import com.github.gdefacci.briscola.service.GameAppModule
import com.github.gdefacci.di.IOC
import modules._
import com.github.gdefacci.briscola.presentation.BriscolaWebApp
import org.http4s.server.ServerApp

object LaunchBlazeBriscolaWebApp extends ServerApp {

//  val routes = new AppRoutes(new Resources(Authority("localhost", 8080), Path / "app", RoutesServletConfig))
//  lazy val gameApp = {
//    import com.github.gdefacci.briscola.service.impl    
//    IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)
//  }
//  val webApp = new BriscolaWebApp(routes, gameApp)
  
  import com.github.gdefacci.briscola.service.impl    
  
  val webApp = IOC.get[BriscolaWebApp](
      new GameLayerModule(IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)),
      ConfModule,
      WebModules)

  def mountServices(plans: Seq[Plan]): BlazeBuilder => BlazeBuilder = { builder =>
    plans.foldLeft(builder) { (builde, plan) =>
      builder.mountService(plan.plan, "/")
    }
  }
  
  val config = mountServices(webApp.plans)
  
  /*
   * object BlazeExample extends ServerApp {
  def server(args: List[String]) = BlazeBuilder.bindHttp(8080)
    .mountService(ExampleService.service, "/http4s")
    .start
}
   */

  def server(args: List[String]) =  BlazeBuilder.bindHttp(8080).withWebSockets(true).mountService(Static.plan, "/").start

}  