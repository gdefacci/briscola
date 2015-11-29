package org.obl.briscola.web.util

import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.http4s.servlet.Http4sServlet

import scala.language.implicitConversions 

class JettyPlanAdder(context:ServletContextHandler) {
  
  def addPlan(plan:ServletPlan) = {
    val pth = plan.servletPath.path.mkString("/")
    println("*"*80)
    val servletPath = s"/$pth/*"
    println(s"adding plan ${plan.getClass.getName} at path '$servletPath'")
    context.addServlet(new ServletHolder(new Http4sServlet(plan.plan)), servletPath)
  }
  
}

object JettyPlanAdder {
  implicit def toJettyPlanAdder(context:ServletContextHandler) = new JettyPlanAdder(context)
}

