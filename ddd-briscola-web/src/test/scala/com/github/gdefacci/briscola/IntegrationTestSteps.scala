package com.github.gdefacci.briscola

import language.implicitConversions

import com.github.gdefacci.briscola.presentation.BriscolaWebApp
import com.github.gdefacci.briscola.service.GameApp
import com.github.gdefacci.bdd.BDD

import scalaz.{ \/-, \/, -\/ }
import com.github.gdefacci.bdd.TestResult
import org.obl.raz._
import rx.lang.scala.Observable
import com.github.gdefacci.briscola.web.util._
import javax.servlet.ServletContext
import com.github.gdefacci.bdd.Fail
import javax.websocket.server.ServerContainer
import com.github.gdefacci.bdd.Ok
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scalaz.Monad
import com.github.gdefacci.briscola.presentation.AppRoutes
import com.github.gdefacci.di.runtime.Bind
import com.github.gdefacci.briscola.presentation.player.Player
import com.github.gdefacci.briscola.presentation.sitemap.SiteMap
import com.github.gdefacci.free._

case class IntegrationTestContext(
  host: Authority,
  contextPath: Path,
  webApp: BriscolaWebApp,
  playerChannels: Path => Throwable \/ Observable[String],
  clientRunner: () => InterpreterJettyRunner)

object IntegratioTestContextModule {

  def playerChannels(webApp: BriscolaWebApp, appRoutes: AppRoutes): Path => Throwable \/ Observable[String] = { uri =>
    appRoutes.playerWebSocketRoutes.PlayerById.fullPath.decodeFull(uri).map { pid =>
      webApp.channel(pid)
    }
  }

  def createJettyWebAppConfig(host: Authority, contextPath: Path, webApp: BriscolaWebApp) = {
    val context = JettyWebAppConfig.defaultWebAppContext()
    context.setContextPath(contextPath.render);
    JettyWebAppConfig.apply(host.port, context, new Containerconfigurator {
      def configureWerbSockets(container: ServerContainer) = {}
      def configureWeb(context: ServletContext) = {
        new ServletContextPlanAdder(context).addPlans(webApp.plans)
      }
    })
  }

  def interpreterJettyRunner(playerChannels: Path => Throwable \/ Observable[String], jettyCfg: JettyWebAppConfig): InterpreterJettyRunner =
    new InterpreterJettyRunner({ () =>
      \/.fromTryCatchNonFatal {
        val interpreter = new TestInterpreter(playerChannels)
        val server = JettyServerFactory.createWebServer(jettyCfg)
        interpreter -> server
      }
    })

  val bindInterpreterFactory = Bind.bind[() => InterpreterJettyRunner]
}


object InitialDataModule {
  
  def emptyMap[A,B] = Map.empty[A,B] 
  def emptyOption[A]:Option[A] = None
}

trait IntegrationTestState {

  def context: IntegrationTestContext

  def http[B](step: ClientStep.Free[B]): B = {
    context.clientRunner().apply(step).get
  }
}

trait IntegrationTestSteps[T <: IntegrationTestState] extends BDD[T, String] {

  def can(expectation: Expectation): Expectation = expectation
  def cant(exp: Expectation): Expectation = expectations { i =>
    (exp.predicate(i).map {
      case Ok => Fail(s"Expecting '${exp.description.mkString(" and ")}' to fail but succeed")
      case _ => Ok
    })
  }
  def can(exp: Step): Step = exp
  
  def cant(exp: Step): Step = step { i =>
    Try(exp.run(i)) match {
      case Success(res) => throw new RuntimeException(s"Expecting '${exp.description.mkString(" and ")}' to fail but got $res")
      case Failure(err) => i
    }
  }

 private def repeatStep(stp: Step, until: T => Boolean): T => T = { st =>
    Try(stp.run(st)).map { nst =>
      if (until(nst)) st
      else repeatStep(stp, until)(nst)
    }.get
  }

  def repeat(step: Step, until: T => Boolean): Step = this.step(repeatStep(step, until))

  
}

trait BaseTestState extends IntegrationTestState {
  def siteMap: SiteMap
  def players: Map[String, Player]
}

trait BaseSteps[S <: BaseTestState] extends IntegrationTestSteps[S] {
  import ClientStepFactory._

  lazy val testClient = new TestClient {}

  def `player received message`(player: String, predicate: String => Boolean): Expectation = expectation { st =>
    st.http(testClient.playerReceivedMessage(st.players(player), predicate))
  }

  def `players received message`(players: Seq[String], predicate: String => Boolean): Expectation = expectations { st =>
    st.http(testClient.playersReceivedMessage(players.map(player => st.players(player)), predicate))
  }

  def `all players received message`(predicate: String => Boolean): Expectation = expectations { st =>
    st.http(testClient.playersReceivedMessage(st.players.values.toSeq, predicate))
  }

  def `player starts match`(player: String, otherPlayers: Seq[String]): Step = step { state =>
    state.http {
      testClient.playerStartsCompetition(state.players(player), otherPlayers.map(p=>state.players(p)), """ "single-match" """, """ "all-players" """).map(_ => state)
    }
  }

  def `player starts a "on player count" match`(player: String, otherPlayers: Seq[String], playerCount: Int): Step = step { state =>
    state.http {
      testClient.playerStartsCompetition(
          state.players(player), 
          otherPlayers.map(p=>state.players(p)), 
          """ "single-match" """, 
          s"""{ "count":$playerCount }"""
      ).map(_ => state)
    }
  }

}
