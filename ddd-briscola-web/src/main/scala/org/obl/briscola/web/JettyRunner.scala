package org.obl.briscola.web

import org.obl.briscola.web.util.JettyServerFactory
import org.obl.briscola.web.util.JettyWebAppConfig
import org.obl.raz.Authority
import org.obl.raz.Path


object AppConfigFactory {

  val authority = Authority("localhost", 8080)
  val contextPath = Path / "app"
  val resources = new Resources(authority, contextPath, RoutesServletConfig)
  
  lazy val webAppConfig = new BriscolaWebAppConfig(new AppRoutes(resources), org.obl.briscola.service.Config.simple.app)

  class ConcretePlayerWebSocketEndPoint extends webAppConfig.ConfiguredPlayerWebSocketEndPoint

  lazy val configurator = new BriscolaContainerConfigurator[ConcretePlayerWebSocketEndPoint](webAppConfig)

  lazy val create: JettyWebAppConfig = {
    val context = JettyWebAppConfig.defaultWebAppContext()
    context.setContextPath(contextPath.render);
    JettyWebAppConfig(authority.port, context, configurator)
  }
}

object JettyRunner extends App {

  val appcfg = AppConfigFactory
  
  val (server,_) = JettyServerFactory.createServers(appcfg.create)

  server.start();
  server.dump(System.err);
  server.join();

}