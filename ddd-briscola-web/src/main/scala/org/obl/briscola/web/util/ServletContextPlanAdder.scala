package org.obl.briscola.web.util

import org.eclipse.jetty.servlet.ServletHolder
import org.http4s.servlet.Http4sServlet
import scala.language.implicitConversions
import javax.servlet.ServletContext
import javax.websocket.server.ServerContainer

class ServletContextPlanAdder(context:ServletContext) {
  
  def addPlan(plan:Plan) = {
    val pth = plan.servletPath.path.mkString("/")
    println("*"*80)
    val servletPath = s"/$pth/*"
    println(s"adding plan ${plan.getClass.getName} at path '$servletPath'")
    val cfg = context.addServlet(plan.getClass.getName, new Http4sServlet(plan.plan))
    cfg.addMapping(servletPath)
    cfg.setAsyncSupported(true)
  }
  
}

object ServletContextPlanAdder {
  implicit def toJettyPlanAdder(context:ServletContext) = new ServletContextPlanAdder(context)
}

trait Containerconfigurator {
  def configureWerbSockets(container: ServerContainer) 
  def configureWeb(context: ServletContext) 
}