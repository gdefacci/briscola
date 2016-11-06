package com.github.gdefacci.briscola.web.util

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer
import com.github.gdefacci.briscola.web.util.ServletContextPlanAdder.toServletPlanAdder

import javax.servlet.ServletContext
import javax.websocket.server.ServerContainer

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
    val allPlans = plan +: plans
    apply(port, contextPath, allPlans)    
  }
  
  def apply(port:Int, contextPath:String, allPlans:Seq[ServletPlan]):JettyWebAppConfig = {
    val context = JettyWebAppConfig.defaultWebAppContext()
    context.setContextPath((if (contextPath.startsWith("/")) "" else "/") + contextPath);
    JettyWebAppConfig(port, context, new Containerconfigurator {
      def configureWerbSockets(container: ServerContainer) = {}
      def configureWeb(context: ServletContext) = {
        allPlans.foreach(context.addPlan(_))
      }
    })
  }
}

object JettyServerFactory {

  def createServers(jettyConfig: => JettyWebAppConfig): (Server, org.eclipse.jetty.websocket.jsr356.server.ServerContainer) = {
    val server = new Server();

    val connector = new ServerConnector(server);
    connector.setPort(jettyConfig.port);
    server.addConnector(connector);
    server.setStopAtShutdown(true)

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
      def lifeCycleStopped(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {
      }
      def lifeCycleStopping(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {}
    })

    server -> wscontainer
  }
  
  def createWebServer(jettyConfig: => JettyWebAppConfig): Server = {
    val server = new Server();

    val connector = new ServerConnector(server);
    connector.setPort(jettyConfig.port);
    server.addConnector(connector);
    server.setStopAtShutdown(true)
    
    val context = jettyConfig.context

    server.setHandler(context);

    context.addLifeCycleListener(new LifeCycle.Listener {
      def lifeCycleFailure(l: org.eclipse.jetty.util.component.LifeCycle, err: Throwable): Unit = {}
      def lifeCycleStarted(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {}
      def lifeCycleStarting(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {
        jettyConfig.configurator.configureWeb(context.getServletContext)
      }
      def lifeCycleStopped(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {
      }
      def lifeCycleStopping(l: org.eclipse.jetty.util.component.LifeCycle): Unit = {}
    })

    server 
  }


}
