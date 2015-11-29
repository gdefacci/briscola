package org.obl.briscola.web.util

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.obl.briscola.web.JettyWebAppConfig
import org.obl.briscola.web.JettyServerFactory

trait JettySpec {

  def jettyConfig: JettyWebAppConfig

  private lazy val server = JettyServerFactory.createServer(jettyConfig)

  private val serverThread = new Thread() {

    override def run() {
      server.start()
      server.join()
    }

    def done() {
      server.stop()
    }

    def isStarted = server.isStarted()

  }

  def stopServer() = {
    server.stop()
  }

  lazy val host = server.getURI.getHost
  
  lazy val contextPath = server.getURI 
  
  def startServer() {
    serverThread.start()
    while (!server.isStarted)
      Thread.sleep(10)
  }
}