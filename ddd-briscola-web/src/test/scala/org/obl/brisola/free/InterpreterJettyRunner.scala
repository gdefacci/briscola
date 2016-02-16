package org.obl.brisola.free

import org.eclipse.jetty.server.Server

class InterpreterJettyRunner(interpreter: TestInterpreter, createServer: () => Server) {

  def run(test: Step.FreeStep[Any]): Seq[TestResult] = {
    var server: Server = null
    try {
      server = createServer()
      server.start()
      interpreter(test)
    } finally {
      if (server != null && !server.isStopped()) server.stop()
    }
  }

}