package org.obl.briscola.web.util

import org.http4s.servlet.Http4sServlet
import scala.language.implicitConversions
import javax.servlet.ServletContext
import javax.websocket.server.ServerContainer
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import javax.websocket.Endpoint
import org.obl.raz.UriTemplate
import scala.reflect.ClassTag

class ServletContextPlanAdder(context:ServletContext) {
  
  lazy val logger = Logger(LoggerFactory.getLogger(getClass))
  
  def addPlan(plan:ServletPlan) = {
    val pth = plan.servletPath.render
    logger.debug("*"*80)
    val servletPath = s"$pth/*"
    logger.debug(s"adding plan ${plan.getClass.getName} at path '$servletPath'")
    val cfg = context.addServlet(plan.getClass.getName, new Http4sServlet(plan.plan))
    cfg.addMapping(servletPath)
    cfg.setAsyncSupported(true)
  }
  
  def addPlans(plans:Seq[ServletPlan]):Unit = {
    plans.foreach(addPlan(_))
  }
  
}

object ServletContextPlanAdder {
  implicit def toServletPlanAdder(context:ServletContext) = new ServletContextPlanAdder(context)
}

trait Containerconfigurator {
  def configureWerbSockets(container: ServerContainer) 
  def configureWeb(context: ServletContext) 
}

class ContainerConfiguratorImpl[T <: Endpoint](webSocketUriTemplate:UriTemplate, plans: => Seq[ServletPlan])(implicit classTag: ClassTag[T]) extends Containerconfigurator {

  import ServletContextPlanAdder._
  import WSEndPointAdder._
  
  def configureWerbSockets(container: ServerContainer) = {
    container.addWebSocketEndPoint[T](webSocketUriTemplate)
  }

  def configureWeb(context: ServletContext) = {
    context.addPlans(plans)
  }

}