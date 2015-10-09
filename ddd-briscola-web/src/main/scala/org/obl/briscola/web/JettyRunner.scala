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
import org.obl.briscola.web.util.JettyPlanAdder._
import org.obl.briscola.web.util.WSEndPointAdder._
import org.obl.briscola.service._

object JettyRunner extends App {

  val routesConfig = WebAppConfig.development
  val app = org.obl.briscola.service.Config.simple.app
  val routes = AppRoutesImpl(routesConfig)
  val webApp = new BriscolaWebApp(routes, app)

  class ConcretePlayerWebSocketEndPoint extends PlayerWebSocketEndPoint(routesConfig.contextPath,
    routes.playerRoutes,
    app.playerService,
    app.gameService,
    app.competitionService,
    new PlayersStateChangeFilter(webApp.playerPresentationAdapter),
    new GamesStateChangeFilter(app.gameService, webApp.gamePresentationAdapter),
    new CompetitionsStateChangeFilter(app.competitionService, webApp.competitionPresentationAdapter))

  def configureWerbSockets(container: ServerContainer) = {
    container.addWebSocketEndPoint[ConcretePlayerWebSocketEndPoint](routes.playerRoutes.playerWebSocketUriTemplate)
  }

  def configureWeb(context: ServletContextHandler) = {
    context.addPlan(webApp.competitionsPlan)
    context.addPlan(webApp.gamesPlan)
    context.addPlan(webApp.playersPlan)
    context.addPlan(webApp.siteMapPlan)
  }

  val server = new Server();

  val connector = new ServerConnector(server);
  connector.setPort(routesConfig.host.port);
  server.addConnector(connector);

  val basePath = "src/main/webapp"

  val context = new WebAppContext();
  context.setResourceBase(basePath);

  context.setContextPath("/" + routesConfig.contextPath.path.mkString("/"));
  context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
  context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");

  configureWeb(context)

  server.setHandler(context);

  val wscontainer = WebSocketServerContainerInitializer.configureContext(context);

  wscontainer.setDefaultMaxSessionIdleTimeout(0)
  configureWerbSockets(wscontainer)

  server.start();
  server.dump(System.err);
  server.join();

}