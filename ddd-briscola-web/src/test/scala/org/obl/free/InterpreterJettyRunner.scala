package org.obl.free

import org.eclipse.jetty.server.Server
import scalaz.{-\/, \/, \/-}
import scala.util.control.NonFatal

class InterpreterJettyRunner[S](create: () => Throwable \/ (TestInterpreterFunction[S], Server) ) extends TestInterpreterFunction[S] {

  def apply(test: Step.Free[S, Any]): Seq[TestResult] = {
    var server: Server = null
    create().map {
      case (interpreter, server1) =>
        try {
          server = server1
          server.start()
          interpreter(test)
        } catch {
          case NonFatal(e) => Seq(Error(e))
        } finally {
          if (server != null) stopServer(server)
        }
    } match {
      case -\/(err) => Seq(Error(new RuntimeException("initialization error ", err)))
      case \/-(v) => v
    }
  }
  
  def stopServer(server:Server):Unit =  {
    val hasStopped = server.isStopped()
    if (!hasStopped) {
      server.stop()
      server.destroy();
      server.getThreadPool().join();
      stopServer(server)
    }
  }

}