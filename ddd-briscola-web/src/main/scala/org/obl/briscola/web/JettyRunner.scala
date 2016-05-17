package org.obl.briscola.web

import org.obl.briscola.web.util.JettyServerFactory
import org.obl.briscola.web.util.JettyWebAppConfig
import org.obl.raz.Authority
import org.obl.raz.Path
import org.obl.briscola.web.util.ContainerConfiguratorImpl
import org.obl.briscola.service.BriscolaApp

object AppConfigFactory {

  val authority = Authority("localhost", 8080)
  val contextPath = Path / "app"
  val resources = new Resources(authority, contextPath, RoutesServletConfig)
  
  lazy val webAppConfig = new BriscolaWebAppConfig(new AppRoutes(resources), BriscolaApp.simple)

  class ConcretePlayerWebSocketEndPoint extends webAppConfig.ConfiguredPlayerWebSocketEndPoint

  lazy val configurator = new ContainerConfiguratorImpl[ConcretePlayerWebSocketEndPoint](
      webAppConfig.routes.playerWebSocketRoutes.playerByIdUriTemplate, webAppConfig.webApp.plans)

}

object JettyRunner extends App {

  val appcfg = AppConfigFactory
  
  lazy val createJettyWebAppConfig: JettyWebAppConfig = {
    val context = JettyWebAppConfig.defaultWebAppContext()
    context.setContextPath(appcfg.contextPath.render);
    JettyWebAppConfig(appcfg.authority.port, context, appcfg.configurator)
  }
  
  val (server,_) = JettyServerFactory.createServers(createJettyWebAppConfig)

  server.start();
  server.dump(System.err);
  server.join();

}