package com.github.gdefacci.briscola
package web

import org.obl.raz._
import javax.servlet.{ServletContextEvent, ServletContextListener}
import javax.servlet.annotation.WebListener
import javax.websocket.server.{ServerContainer, ServerEndpointConfig}
import org.http4s.HttpService
import org.http4s.server._
import org.http4s.Service
import com.github.gdefacci.di.IOC
import com.github.gdefacci.briscola.service.GameApp
import com.github.gdefacci.briscola.service.GameAppModule

import modules._
import com.github.gdefacci.briscola.presentation.BriscolaWebApp

@WebListener
class Bootstrap extends ServletContextListener {
//  val routes = new AppRoutes(new Resources(Authority("localHost",8080), Path / "app", RoutesServletConfig))
//  
//  lazy val gameApp = {
//    import com.github.gdefacci.briscola.service.impl    
//    IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)
//  }
//
//  val webApp = new BriscolaWebApp(routes, gameApp)
//  
//  lazy val plans = http4sService(Seq(Static.plan) ++ 
//      webApp.SiteMap.plans.map(_.plan) ++ webApp.Players.plans.map(_.plan) ++ webApp.Games.plans.map(_.plan) ++ webApp.Competitions.plans.map(_.plan))
  
  import com.github.gdefacci.briscola.service.impl    

  val webApp = IOC.get[BriscolaWebApp](
      new GameLayerModule(IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)),
      ConfModule,
      WebModules)

  import org.http4s.servlet.syntax._

  def http4sService(servcs:Seq[HttpService]):HttpService = {
    servcs match {
      case Nil => throw new Exception("")
      case hd :: Seq() => hd 
      case hd :: rest => 
        Service.withFallback( http4sService(rest) )(hd)
    }
  }
  
  /*
   *  lazy val plans = http4sService(Seq(Static.plan) ++ 
      webApp.SiteMap.plans.map(_.plan) ++ webApp.Players.plans.map(_.plan) ++ webApp.Games.plans.map(_.plan) ++ webApp.Competitions.plans.map(_.plan))

  i
   */
  
  override def contextInitialized(sce: ServletContextEvent): Unit = {
    val ctx = sce.getServletContext
    val wsCont: ServerContainer = ctx.getAttribute("javax.websocket.server.ServerContainer").asInstanceOf[ServerContainer]
    ctx.mountService("plans", http4sService(Seq(Static.plan) ++ webApp.plans.map(_.plan)), "/*")
  }

  override def contextDestroyed(sce: ServletContextEvent): Unit = {}
}
