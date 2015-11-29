package org.obl.briscola.web

import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer
import javax.websocket.server.ServerContainer
import javax.websocket.server.ServerEndpointConfig
import org.obl.raz._
import org.http4s.server._
import org.http4s.servlet.Http4sServlet
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.webapp.WebAppContext
import org.obl.briscola.web.util.ServletContextPlanAdder._
import org.obl.briscola.web.util.WSEndPointAdder._
import org.obl.briscola.service._
import javax.servlet.ServletContext
import org.eclipse.jetty.util.component.LifeCycle
import org.obl.briscola.web.util.Containerconfigurator
import org.obl.briscola.web.util.Plan
import org.obl.briscola.web.util.ServletPlan

import org.obl.briscola.web.util.{JettyWebAppConfig, JettyServerFactory}


object AppConfigFactory {

  val webAppConfig = new BriscolaWebAppConfig(WebAppConfig.development, org.obl.briscola.service.Config.simple.app)

  class ConcretePlayerWebSocketEndPoint extends webAppConfig.ConfiguredPlayerWebSocketEndPoint

  val configurator = new BriscolaContainerConfigurator[ConcretePlayerWebSocketEndPoint](webAppConfig)

  def create: JettyWebAppConfig = {
    val context = JettyWebAppConfig.defaultWebAppContext()
    context.setContextPath("/" + webAppConfig.routesConfig.contextPath.path.mkString("/"));
    JettyWebAppConfig(webAppConfig.routesConfig.host.port, context, configurator)
  }
}

object JettyRunner extends App {

  val server = JettyServerFactory.createServer(AppConfigFactory.create)

  server.start();
  server.dump(System.err);
  server.join();

}