package org.obl.brisola.integration

import org.obl.briscola.web.util.JettyWebAppConfig
import org.obl.briscola.web.util.JettyServerFactory
import org.obl.briscola.web.BriscolaWebApp
import rx.lang.scala.Observable
import org.obl.raz.Path
import scalaz.\/
import org.obl.briscola.web.util.Containerconfigurator
import javax.websocket.server.ServerContainer
import javax.servlet.ServletContext
import org.obl.briscola.web.util.ServletContextPlanAdder
import org.obl.free._
import scalaz.Free
import scalaz.WriterT

object BriscolaIntegrationTest {
  type ErrorDisjunction[T] = Throwable \/ T
  
  type TestState[S] = scalaz.StateT[ErrorDisjunction, BriscolaWebApp, S]
  
  def TestState[S](f: BriscolaWebApp => ErrorDisjunction[(BriscolaWebApp, S)]): TestState[S] = 
      scalaz.StateT[ErrorDisjunction, BriscolaWebApp, S](f)
      
  type Background[T] = WriterT[TestState, List[String], T]
}

trait BriscolaIntegrationTest[S] extends ScenariosRunner[S] {
  
  import BriscolaIntegrationTest._

  private def createWebApp = TestAppConfig.simpleWebApp

  private def playerChannels(app: BriscolaWebApp): Path => Throwable \/ Observable[String] = { uri =>
    app.routes.playerWebSocketRoutes.PlayerById.fullPath.decodeFull(uri).map { pid =>
      app.competitionPlayerChannels(pid).merge(app.gamePlayerChannels(pid)).merge(app.playerPlayerChannels(pid))
    }
  }
  
  def background:Background[S] 
  
  protected def mountPlans(context: ServletContextPlanAdder, webApp:BriscolaWebApp)

  protected def createJettyWebAppConfig(webApp:BriscolaWebApp) = {
    val context = JettyWebAppConfig.defaultWebAppContext()
    context.setContextPath(TestAppConfig.contextPath.render);
    JettyWebAppConfig.apply(TestAppConfig.authority.port, context, new Containerconfigurator {
      def configureWerbSockets(container: ServerContainer) = {} 
      def configureWeb(context: ServletContext)  = {
        mountPlans(new ServletContextPlanAdder(context), webApp)
      }
    })
  }
  
  lazy val runner:Step.Free[S, Any] => Seq[TestResult] = {
    new InterpreterJettyRunner({ () =>
      background.run(createWebApp).map {
        case (webApp, initState) =>
          val interpreter = new TestInterpreter[S](playerChannels(webApp), initState)
          val jettyCfg = createJettyWebAppConfig(webApp)
          val server = JettyServerFactory.createWebServer(jettyCfg)
          interpreter -> server
      }
    })
  }

}

