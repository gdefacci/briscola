package com.github.gdefacci.free

import org.eclipse.jetty.server.Server
import scalaz.{-\/, \/, \/-}
import scala.util.control.NonFatal
import com.github.gdefacci.bdd.{TestResult, Fail}
import scala.util.Try
import scala.util.Failure
import scala.util.Success

class InterpreterJettyRunner(create: () => Throwable \/ (InterpreterFunction, Server) ) extends InterpreterFunction {

  def apply[T](test: ClientStep.Free[T]): Try[T]= {
    var server: Server = null
    create().map {
      case (interpreter, server1) =>
        try {
          server = server1
          server.start()
          interpreter[T](test)
        } catch {
          case NonFatal(e) => Failure(e)
        } finally {
          if (server != null) stopServer(server)
        }
    } match {
      case -\/(err) => Failure(new RuntimeException("initialization error ", err))
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