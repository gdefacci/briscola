package org.obl.briscola.web.util

import javax.websocket.server.ServerContainer
import javax.websocket.Endpoint
import org.obl.raz.UriTemplate
import scala.reflect.ClassTag
import javax.websocket.server.ServerEndpointConfig

import scala.language.implicitConversions 

class WSEndPointAdder(container:ServerContainer) {
  
  def addWebSocketEndPoint[T <: Endpoint](ut:UriTemplate)(implicit ct:ClassTag[T]) = {
    val wsPathStr = ut.render
    
    println("*"*80)
    println(s"adding websocket ${ct.runtimeClass.getName} at path '${wsPathStr}'")
    println("")
    val config = ServerEndpointConfig.Builder.create(
        ct.runtimeClass, 
        wsPathStr ).build
        
    container.addEndpoint(config)
    
  }
  
}

object WSEndPointAdder {
  
  implicit def toWSEndPointAdder(container:ServerContainer) = new WSEndPointAdder(container)
}