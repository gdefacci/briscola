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

trait IntegrationTestState {

  def context: IntegrationTestContext

  def http[B](step: ClientStep.Free[B]): Try[B] = {
    context.clientRunner().apply(step)
  }
}

trait IntegrationTestSteps[T <: IntegrationTestState] extends BDD[T, Try, String] {

  def toTry[E, T](d: E \/ T): Try[T] = d match {
    case -\/(err) => Failure(new RuntimeException(err.toString()))
    case \/-(v) => Success(v)
  }

  implicit val tryMonad = new Monad[Try] {
    def bind[A, B](fa: Try[A])(f: A => Try[B]): Try[B] = fa.flatMap(f)
    def point[A](a: => A): Try[A] = Try(a)
  }

  def can(expectation: Expectation): Expectation = expectation
  def cant(exp: Expectation): Expectation = expectations { i =>
    (exp.predicate(i).map {
      case Ok => Fail(s"Expecting '${exp.description.mkString(" and ")}' to fail but succeed")
      case _ => Ok
    })
  }
  def can(exp: Step): Expectation = expectation { i =>
    i.flatMap(exp.run) match {
      case Success(res) => Ok
      case Failure(err) => Fail(err.getMessage + "\n\n" + err.getStackTrace.mkString("\n") + "\n")
    }
  }
  def cant(exp: Step): Expectation = expectation { i =>
    i.flatMap(exp.run) match {
      case Success(res) => Fail(s"Expecting '${exp.description.mkString(" and ")}' to fail but got $res")
      case Failure(err) => Ok
    }
  }

  def toTestResult(t: Try[TestResult[String]]): TestResult[String] = t match {
    case Success(res) => res
    case Failure(err) => Fail(err.getMessage + "\n\n" + err.getStackTrace.mkString("\n") + "\n")
  }

  def toTestResults(t: Try[Seq[TestResult[String]]]): List[TestResult[String]] = t match {
    case Success(res) => res.toList
    case Failure(err) => Fail(err.getMessage + "\n\n" + err.getStackTrace.mkString("\n") + "\n") :: Nil
  }

  def httpExpect(f: T => ClientStep.Free[TestResult[String]]): Try[T] => TestResult[String] = { st =>
    toTestResult(st.flatMap { st =>
      st.http(f(st))
    })
  }

  def httpExpectList(f: T => ClientStep.Free[List[TestResult[String]]]): Try[T] => List[TestResult[String]] = { st =>
    toTestResults(st.flatMap { st =>
      st.http(f(st))
    })
  }

}

trait BaseTestState extends IntegrationTestState {
  def siteMap: SiteMap
  def players: Map[String, Player]
}

trait BaseSteps[S <: BaseTestState] extends IntegrationTestSteps[S] {
  import ClientStepFactory._

  import TestDecoders.PrivatePlayer._

  lazy val testClient = new TestClient {}

  def `player received message`(player: String, predicate: String => Boolean): Expectation = expectation(httpExpect { st =>
    testClient.playerReceivedMessage(st.players(player), predicate)
  })

  def `players received message`(players: Seq[String], predicate: String => Boolean): Expectation = expectations(httpExpectList { st =>
    testClient.playersReceivedMessage(players.map(player => st.players(player)), predicate)
  })

  def `all players received message`(predicate: String => Boolean): Expectation = expectations(httpExpectList { st =>
    testClient.playersReceivedMessage(st.players.values.toSeq, predicate)
  })

  def `player starts match`(player: String, otherPlayers: Seq[String]): Step = step { state =>
    state.http {
      testClient.playerStartsCompetition(state.players(player), otherPlayers.map(p=>state.players(p)), """ "single-match" """, """ "all-players" """).map(_ => state)
//      for {
//        resp <- post(state.players(player).createCompetition, s"""{
//          "players":[${otherPlayers.map(p => s""""${state.players(p).self.render}"""").mkString(",")}],
//          "kind":"single-match",
//          "deadline":"all-players"
//        }""")
//        _ <- check(if (resp.is2xx) Ok else Fail(s"cant create competition ${resp.body}"))
//      } yield state
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
//      for {
//        resp <- post(state.players(player).createCompetition, s"""{
//        "players":[${otherPlayers.map(p => s""""${state.players(p).self.render}"""").mkString(",")}],
//        "kind":"single-match",
//        "deadline":{
//          "count":$playerCount
//        }
//      }""")
//        _ <- check(if (resp.is2xx) Ok else Fail(s"cant create competition ${resp.body}"))
//      } yield state
    }
  }

}
