package org.obl.brisola.free

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer

class InterpreterJettyRunner(interpreter: TestInterpreter, createServer: () => (Server, ServerContainer)) extends (Step.FreeStep[Any] => Seq[TestResult]) {

  def apply(test: Step.FreeStep[Any]): Seq[TestResult] = {
    var server: Server = null
    var wsServer: ServerContainer = null
    try {
      val p = createServer()
      server = p._1
      wsServer = p._2
      server.start()
      interpreter(test)
    } finally {
      if (server != null && !server.isStopped()) {
        server.stop()
      }
      if (wsServer != null) {
        wsServer.stop()
      }
    }
  }

}