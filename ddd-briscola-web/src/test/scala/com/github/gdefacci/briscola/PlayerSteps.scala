package com.github.gdefacci.briscola

import com.github.gdefacci.briscola.presentation.BriscolaWebApp
import com.github.gdefacci.briscola.presentation.AppRoutes

import scalaz.{ \/-, \/, -\/ }
import org.obl.raz.Path
import rx.lang.scala.Observable
import com.github.gdefacci.briscola.service.AppServices
import org.obl.raz.Authority
import com.github.gdefacci.di.IOC

import com.github.gdefacci.free._
import com.github.gdefacci.bdd._
import com.github.gdefacci.briscola.presentation.sitemap.SiteMap
import com.github.gdefacci.briscola.player.PlayerId
import scala.util.Failure
import scala.util.Success

case class PlayerIntegrationTestState(
  context: IntegrationTestContext,
  siteMap: SiteMap,
  services: AppServices) extends IntegrationTestState

class PlayerSteps extends IntegrationTestSteps[PlayerIntegrationTestState] {

  import ClientStepFactory._

  lazy val contextPath: Path = TestConfModule.contextPath
  lazy val authority: Authority = TestConfModule.authority

  lazy val `given an initial application state`: Source = source { () =>
    IOC.get[PlayerIntegrationTestState](new IntegrationTestModule, IntegratioTestContextModule)
  }

  def `is created player`(name: String, password: String): Step = step { state =>
    state.services.player.createPlayer(name, password).map(_ => state).toOption.get
  }

  def `create player`(name: String, password: String): Expectation = expectation { state =>
    state.http(
      for {
        playerResp1 <- post(state.siteMap.players, s"""{ "name":"${name}", "password":"${password}" }""")
      } yield if (playerResp1.is2xx) Ok else Fail(s"cant create player $name, cause  ${playerResp1.body}"))
  }

}