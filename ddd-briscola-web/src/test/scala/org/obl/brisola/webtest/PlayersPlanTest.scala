package org.obl.brisola.webtest

import org.obl.briscola.presentation
import org.obl.briscola.web.util.JettySpec
import org.obl.briscola.web.AppConfigFactory
import scala.concurrent.{ Await }
import scala.concurrent.duration._
import dispatch._
import Defaults._
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span
import org.scalatest.time.Seconds
import org.scalatest.time.Millis
import org.obl.briscola.web.PlayersPlan
import org.obl.briscola.web.BriscolaWebAppConfig
import org.obl.briscola.web.WebAppConfig
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola.web.RoutesServletConfig
import org.obl.briscola.web.WebAppConfig.DefaultRoutesConfig
import org.obl.raz.HTTP
import org.obl.raz.PathSg
import org.scalatest.BeforeAndAfterAll
import org.obl.briscola.web.util.JettyWebAppConfig
import org.obl.briscola.web.util.ServletPlan
import scala.util.Try
import scala.util.Success
import argonaut.DecodeJson
import com.ning.http.client.Response
import scala.util.Failure
import org.scalatest.BeforeAndAfterEach

abstract class E2ETest(val secondsTimeout: Int = 1000) extends FunSuite with JettySpec with BeforeAndAfterAll with ScalaFutures with TestDecoders {

  lazy val testContextPath = "test"

  lazy val testServletConfig = new RoutesServletConfig with DefaultRoutesConfig {
    val host = HTTP("localhost", 8080)
    val contextPath = PathSg(testContextPath)
  }

  implicit lazy val defaultPatience =
    PatienceConfig(timeout = Span(secondsTimeout, Seconds), interval = Span(100, Millis))

  override def beforeAll() {
    startServer()
    setup
  }

  override def afterAll() {
    stopServer()
  }

  def setup: Unit
}

trait TestClient { self: E2ETest =>

  def getSiteMap: presentation.SiteMap = {
    val siteMapUrl = url(s"${contextPath}/site-map")
    val siteMap = Http(siteMapUrl.GET OK as.String)

    whenReady(siteMap) { result =>
      decode[presentation.SiteMap](result) match {
        case \/-(x) => x
        case _ => fail(s"error decoding SiteMap $result")
      }
    }
  }

  def getPlayers(implicit siteMap: presentation.SiteMap): presentation.Collection[OutPlayer] = {
    whenReady(Http(url(siteMap.players.render).GET OK as.String)) { result =>
      decode[presentation.Collection[OutPlayer]](result)(playersDecode) match {
        case -\/(err) => fail(s"cant decode to OutPlayer] ${result} : ${err}")
        case \/-(v) => v
      }
    }
  }

  def responseDescription(resp: Response) =
    s"""
statusCode : ${resp.getStatusCode}
body       : 
${resp.getResponseBody}
"""

  def httpCall(req: dispatch.Req): Try[Response] =
    Await.ready(Http(req > (i => i)), Duration(secondsTimeout, SECONDS)).value.get

  def playerRegister(name: String, psw: String)(implicit siteMap: presentation.SiteMap): Try[Response] =
    httpCall {
      url(siteMap.players.render).POST.setContentType("application/json", "UTF-8") << s"""{ "name":"${name}", "password":"${psw}" }"""
    }

  def playerLogOn(name: String, psw: String)(implicit siteMap: presentation.SiteMap): Try[Response] =
    httpCall {
      url(siteMap.playerLogin.render).POST.setContentType("application/json", "UTF-8") << s"""{ "name":"${name}", "password":"${psw}" }"""
    }

  def isSuccessResponse(resp: Response) = resp.getStatusCode >= 200 && resp.getStatusCode < 300

  def successfullResponse[T](resp: Try[Response], desc: String)(implicit dj: DecodeJson[T]): T = resp match {
    case Success(resp) if isSuccessResponse(resp) =>
      val result = resp.getResponseBody
      decode[T](result)(dj).toOption.getOrElse(fail(s"cant decode to private $desc ${result}"))
    case Success(resp) => fail("unexpected response " + responseDescription(resp))
    case Failure(err) => fail(err)
  }

  def playerLogOnSucessfully(name: String, psw: String)(implicit siteMap: presentation.SiteMap): presentation.Player = {
    val pl = successfullResponse[presentation.Player](playerLogOn(name, psw), "player")(privatePlayerDecode)
    assert(pl.name === name)
    pl
  }
  
  def playerRegisterSucessfully(name: String, psw: String)(implicit siteMap: presentation.SiteMap): presentation.Player = {
    val pl = successfullResponse[presentation.Player](playerRegister(name, psw), "player")(privatePlayerDecode)
    assert(pl.name === name)
    pl
  }

  def playerCreateCompetition(players: presentation.Player*)(implicit currentPlayer: presentation.Player): Try[Response] = {
    val playersSet = players.toSet + currentPlayer
    val body = s"""{
  "players":[${players.map(p => s""""${p.self}"""").mkString(",")}],
  "kind":"single-match",
  "deadline":"all-players"
}"""
    httpCall(url(currentPlayer.createCompetition.render).POST.setContentType("application/json", "UTF-8") << body)
  }

  def playerCreateCompetitionSuccessfully(players: presentation.Player*)(implicit currentPlayer: presentation.Player): presentation.CompetitionState = {
    successfullResponse[presentation.CompetitionState](playerCreateCompetition(players: _*), "competition state")
  }

}

trait PlayersPlanTest { self: E2ETest =>

  lazy val webAppConfig:BriscolaWebAppConfig = new BriscolaWebAppConfig(testServletConfig, org.obl.briscola.service.Config.createSimpleApp)
  
  def testPlans: Seq[ServletPlan] = {
    Seq(webAppConfig.webApp.siteMapPlan, webAppConfig .webApp.playersPlan)
  }

  def jettyConfig = JettyWebAppConfig(8080, testContextPath, testPlans)

}

class GivenNoPlayers extends E2ETest with PlayersPlanTest with TestClient {
  
  def setup = {}
  
  implicit lazy val siteMap = getSiteMap

  test("a player can register") {
    playerRegisterSucessfully("pippo", "password")
  }
  
}


class GivenOnePlayer extends E2ETest with PlayersPlanTest with TestClient {

  def setup = {
    webAppConfig.webApp.app.playerService.createPlayer("Pippo", "pippo")
  }

  implicit lazy val siteMap = getSiteMap

  test("players collection has size 1") {
    //println(playerRegisterSucessfully("pippo", "password"))
    println(getPlayers.members.mkString("\n"))
    assert(getPlayers.members.size === 1)
  }

  test("player with valid credential can logon") {
    playerLogOnSucessfully("Pippo", "pippo")
  }

  test("player with invalid credential cant logon") {
    assert(!isSuccessResponse(playerLogOn("Pippo", "wrong").get))
  }

  test("A single player cant start a competition") {
    implicit val pl = playerLogOnSucessfully("Pippo", "pippo")
    assert(!isSuccessResponse(playerCreateCompetition(pl).get))
  }
}

class Given2Players extends E2ETest with PlayersPlanTest with TestClient {

  override def testPlans: Seq[ServletPlan] = super.testPlans :+ webAppConfig.webApp.competitionsPlan

  def setup = {
    webAppConfig.webApp.app.playerService.createPlayer("Pippo", "pippo")
    webAppConfig.webApp.app.playerService.createPlayer("Minni", "minni")
  }

  implicit lazy val siteMap = getSiteMap

  test("players collection has size 2") {
    assert(getPlayers(getSiteMap).members.size === 2)
  }

  test("players with valid credential can logon") {
    playerLogOnSucessfully("Pippo", "pippo")
    playerLogOnSucessfully("Minni", "minni")
  }

  test("player with invalid credential cant logon") {
    assert(!isSuccessResponse(playerLogOn("Pippo", "wrong").get))
  }

  test("Pippo can start a competition") {
    implicit val p1 = playerLogOnSucessfully("Pippo", "pippo")
    val p2 = playerLogOnSucessfully("Minni", "minni")
    val comp = playerCreateCompetitionSuccessfully(p1, p2)
    assert(comp.acceptingPlayers.size == 1)
    assert(comp.decliningPlayers.size == 0)
  }

}