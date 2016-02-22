package org.obl.brisola.free

import org.obl.briscola.web.util.JettyWebAppConfig
import org.obl.briscola.web.BriscolaContainerConfigurator
import org.obl.briscola.web.util.JettyServerFactory
import javax.websocket.{EndpointConfig, Endpoint, Session, CloseReason}

object JettyApplicationTestRunner {
  
  object TestWebSocketEndPoint {
    var onOpenAction: (Session, EndpointConfig) => Unit = _
  }
  
  class TestWebSocketEndPointHelper extends Endpoint {
  
    override def onOpen(session: Session, config: EndpointConfig) = {
      TestWebSocketEndPoint.onOpenAction(session, config)
    }
    override def onClose(session: Session, closeReason: CloseReason) = {
    }
    override def onError(session: Session, thr: Throwable) = {
    }
  
  }
}

trait JettyApplicationTestRunner {
  
  import JettyApplicationTestRunner._
  
  def testInterpreterConfig:TestInterpreterConfig
  
  lazy val runner = new InterpreterJettyRunner(
    new TestInterpreter(testInterpreterConfig),
    { () =>
      
      val webAppConfig = TestAppJettyConfig.simpleWebAppConfig

      val delegate = new webAppConfig.ConfiguredPlayerWebSocketEndPoint

      TestWebSocketEndPoint.onOpenAction = (s, c) => delegate.onOpen(s, c)
      val configurator = new BriscolaContainerConfigurator[TestWebSocketEndPointHelper](webAppConfig)

      val jettyCfg = {
        val context = JettyWebAppConfig.defaultWebAppContext()
        context.setContextPath("/" + webAppConfig.routesConfig.contextPath.path.mkString("/"));
        JettyWebAppConfig(webAppConfig.routesConfig.host.port, context, configurator)
      }

      JettyServerFactory.createServers(jettyCfg)
    })
}

