package org.obl.briscola.web

import org.obl.briscola.web.util.JettyServerFactory
import org.obl.briscola.web.util.JettyWebAppConfig


object AppConfigFactory {

  val webAppConfig = new BriscolaWebAppConfig(WebAppConfig.development, org.obl.briscola.service.Config.simple.app)

  class ConcretePlayerWebSocketEndPoint extends webAppConfig.ConfiguredPlayerWebSocketEndPoint

  val configurator = new BriscolaContainerConfigurator[ConcretePlayerWebSocketEndPoint](webAppConfig)

  lazy val create: JettyWebAppConfig = {
    val context = JettyWebAppConfig.defaultWebAppContext()
    context.setContextPath("/" + webAppConfig.routesConfig.contextPath.path.mkString("/"));
    JettyWebAppConfig(webAppConfig.routesConfig.host.port, context, configurator)
  }
}

object JettyRunner extends App {

  val appcfg = AppConfigFactory
  
  val (server,_) = JettyServerFactory.createServers(appcfg.create)

  server.start();
  server.dump(System.err);
  server.join();

}