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

object JettyRunner extends App {

  val webAppConfig = new BriscolaWebAppConfig(WebAppConfig.development, org.obl.briscola.service.Config.simple.app)

  class ConcretePlayerWebSocketEndPoint extends webAppConfig.ConfiguredPlayerWebSocketEndPoint
  
  val configurator = new BriscolaContainerConfigurator[ConcretePlayerWebSocketEndPoint](webAppConfig)

  val server = new Server();

  val connector = new ServerConnector(server);
  connector.setPort(webAppConfig.routesConfig.host.port);
  server.addConnector(connector);

  val basePath = "src/main/webapp"

  val context = new WebAppContext();
  context.setResourceBase(basePath);

  context.setContextPath("/" + webAppConfig.routesConfig.contextPath.path.mkString("/"));
  context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
  context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");

  server.setHandler(context);

  val wscontainer = WebSocketServerContainerInitializer.configureContext(context);
  wscontainer.setDefaultMaxSessionIdleTimeout(0)

  context.addLifeCycleListener(new LifeCycle.Listener {
    def lifeCycleFailure(l: org.eclipse.jetty.util.component.LifeCycle, err: Throwable): Unit = {}
    def lifeCycleStarted(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {}
    def lifeCycleStarting(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {
      configurator.configureWeb(context.getServletContext)
      configurator.configureWerbSockets(wscontainer)
    }
    def lifeCycleStopped(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {}
    def lifeCycleStopping(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {}
  })
  
  server.start();
  server.dump(System.err);
  server.join();

}