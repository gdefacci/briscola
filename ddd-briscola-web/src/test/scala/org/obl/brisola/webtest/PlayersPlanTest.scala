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
import org.obl.briscola.web.JettyWebAppConfig
import org.obl.briscola.web.PlayersPlan
import org.obl.briscola.web.BriscolaWebAppConfig
import org.obl.briscola.web.WebAppConfig
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola.web.RoutesServletConfig
import org.obl.briscola.web.WebAppConfig.DefaultRoutesConfig
import org.obl.raz.HTTP
import org.obl.raz.PathSg
import org.scalatest.BeforeAndAfterAll

abstract class E2ETest(secodsTimeout: Int = 1000) extends FunSuite with JettySpec with BeforeAndAfterAll with ScalaFutures with TestDecoders {

  lazy val testContextPath = "test"

  lazy val testServletConfig = new RoutesServletConfig with DefaultRoutesConfig {
    val host = HTTP("localhost", 8080)
    val contextPath = PathSg(testContextPath)
  }

  implicit lazy val defaultPatience =
    PatienceConfig(timeout = Span(secodsTimeout, Seconds), interval = Span(100, Millis))

  override def beforeAll() {
    startServer()
    setup
  }

  override def afterAll() {
    stopServer()
  }
  
  def setup:Unit
}

trait TestClient { self:E2ETest =>
  
  def getSiteMap:presentation.SiteMap = {
    val siteMapUrl = url(s"${contextPath}/site-map")
    val siteMap = Http(siteMapUrl.GET OK as.String)

    whenReady(siteMap) { result =>
      decode[presentation.SiteMap](result) match {
        case \/-(x) => x
        case _ => fail(s"error decoding SiteMap $result")
      }
    }
  }
  
  def getPlayers:presentation.SiteMap => presentation.Collection[OutPlayer] = { siteMap =>
    whenReady(Http(url(siteMap.players.render).GET OK as.String)) { result =>
      decode[presentation.Collection[OutPlayer]](result)(playersDecode) match {
        case -\/(err) => fail(s"cant decode to OutPlayer] ${result} : ${err}")
        case \/-(v) => v
      }
    }
  }
  
  def playerLogOnSucessfully(name: String, psw: String):presentation.SiteMap => presentation.Player = { siteMap =>
    val loginPlayer = url(siteMap.playerLogin.render).POST.setContentType("application/json", "UTF-8") << s"""{ "name":"${name}", "password":"${psw}" }"""
    whenReady(Http(loginPlayer OK as.String)) { result =>
      val pl = decode[presentation.Player](result)(privatePlayerDecode).toOption.getOrElse(fail(s"cant decode to private player ${result}"))
      assert(pl.name === name)
      pl
    }
  }
  
  def playerLogOnFail(name: String, psw: String):presentation.SiteMap => Future[Throwable] = { siteMap =>
    val loginPlayer = url(siteMap.playerLogin.render).POST.setContentType("application/json", "UTF-8") << s"""{ "name":"${name}", "password":"${psw}" }"""
    val req = Http(loginPlayer)
    
    req.onSuccess { case res => fail("logon should be forbidden") }
    req.failed 
  }
  
  def playerCreateCompetition(players:presentation.Player*):presentation.Player => Unit = { player =>
    val playersSet = players.toSet + player
    val body = s"""{
  "players":[${players.map( p => s"'${p.self}'")}],
  "matchKind":"SingleMatch",
  "deadline":"AllPlayers"
}"""
    
    val createCompetition = url(player.createCompetition.render).POST.setContentType("application/json", "UTF-8") << body
    
    
  }
  
}

trait PlayersPlanTest { self:E2ETest =>

  lazy val webAppConfig = new BriscolaWebAppConfig(testServletConfig, org.obl.briscola.service.Config.simple.app)
  lazy val jettyConfig = JettyWebAppConfig(8080, testContextPath, webAppConfig.webApp.siteMapPlan, webAppConfig.webApp.playersPlan)
  
}

class GivenOnePlayer extends E2ETest with PlayersPlanTest with TestClient {

  def setup = {
    webAppConfig.webApp.app.playerService.createPlayer("Pippo", "pippo")
  }
  
  test("players collection has size 1") {
      assert(getPlayers(getSiteMap).members.size === 1) 
  }
  
  test("player with valid credential can logon") {
    playerLogOnSucessfully("Pippo", "pippo")
  }
  
  test("player with invalid credential cant logon") {
    playerLogOnFail("Pippo", "wrong")
  }

//  test("Pippo cannot start a competition") {
//    playerLogOnFail("Pippo", "wrong")
//  }
}


class Given2Players extends E2ETest with PlayersPlanTest with TestClient {

  def setup = {
    webAppConfig.webApp.app.playerService.createPlayer("Pippo", "pippo")
    webAppConfig.webApp.app.playerService.createPlayer("Minni", "minni")
  }
  
  test("players collection has size 2") {
    assert(getPlayers(getSiteMap).members.size === 2) 
  }
  
  test("players with valid credential can logon") {
    playerLogOnSucessfully("Pippo", "pippo")
    playerLogOnSucessfully("Minni", "minni")
  }
  
  test("player with invalid credential cant logon") {
    playerLogOnFail("Pippo", "wrong")
  }
  
  test("Pippo can start a competition") {
    
  }
  
}