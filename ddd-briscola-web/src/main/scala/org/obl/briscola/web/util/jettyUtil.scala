package org.obl.briscola.web.util

import org.eclipse.jetty.webapp.WebAppContext
import javax.websocket.server.ServerContainer
import javax.servlet.ServletContext
import org.obl.briscola.web.util.ServletContextPlanAdder._
import org.obl.briscola.web.util.WSEndPointAdder._
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer
import org.eclipse.jetty.util.component.LifeCycle

case class JettyWebAppConfig(port: Int, context: WebAppContext, configurator: Containerconfigurator)

object JettyWebAppConfig {
  
  def defaultWebAppContext() = {
    val basePath = "src/main/webapp"
    
    val context = new WebAppContext();
    
    context.setResourceBase(basePath);
    context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
    context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
    context
  }
  
  def apply(port:Int, contextPath:String, plan:ServletPlan, plans:ServletPlan*):JettyWebAppConfig = {
    val context = JettyWebAppConfig.defaultWebAppContext()
    context.setContextPath((if (contextPath.startsWith("/")) "" else "/") + contextPath);
    JettyWebAppConfig(port, context, new Containerconfigurator {
      def configureWerbSockets(container: ServerContainer) = {}
      def configureWeb(context: ServletContext) = {
        context.addPlan(plan)
        plans.foreach(context.addPlan(_))
      }
    })
  }
}

object JettyServerFactory {

  def createServer(jettyConfig: => JettyWebAppConfig): Server = {
    val server = new Server();

    val connector = new ServerConnector(server);
    connector.setPort(jettyConfig.port);
    server.addConnector(connector);

    val context = jettyConfig.context

    server.setHandler(context);

    val wscontainer = WebSocketServerContainerInitializer.configureContext(context);
    wscontainer.setDefaultMaxSessionIdleTimeout(0)

    context.addLifeCycleListener(new LifeCycle.Listener {
      def lifeCycleFailure(l: org.eclipse.jetty.util.component.LifeCycle, err: Throwable): Unit = {}
      def lifeCycleStarted(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {}
      def lifeCycleStarting(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {
        jettyConfig.configurator.configureWeb(context.getServletContext)
        jettyConfig.configurator.configureWerbSockets(wscontainer)
      }
      def lifeCycleStopped(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {}
      def lifeCycleStopping(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {}
    })

    server
  }

}
