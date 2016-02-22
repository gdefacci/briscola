package org.obl.briscola.web.util

import javax.websocket.server.ServerContainer
import javax.websocket.Endpoint
import org.obl.raz.UriTemplate
import scala.reflect.ClassTag
import javax.websocket.server.ServerEndpointConfig
import scala.language.implicitConversions
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

class WSEndPointAdder(container:ServerContainer) {
  
  lazy val log = Logger(LoggerFactory.getLogger(getClass))
  
  def addWebSocketEndPoint[T <: Endpoint](ut:UriTemplate)(implicit ct:ClassTag[T]) = {
    val wsPathStr = ut.render
    
    log.debug("*"*80)
    log.debug(s"adding websocket ${ct.runtimeClass.getName} at path '${wsPathStr}'")
    val config = ServerEndpointConfig.Builder.create(
        ct.runtimeClass, 
        wsPathStr ).build
        
    container.addEndpoint(config)
    
  }
  
}

object WSEndPointAdder {
  
  implicit def toWSEndPointAdder(container:ServerContainer) = new WSEndPointAdder(container)
}