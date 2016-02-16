package org.obl.brisola.free

import org.obl.brisola.webtest.TestDecoders
import org.obl.briscola.presentation.Player
import org.obl.briscola.web.jsonDecoders._
import org.obl.raz.Path
import org.obl.briscola.presentation.SiteMap
import com.ning.http.client.Response
import org.obl.briscola.web.util.JettyServerFactory

object PlayerTest extends TestDecoders with App {

  import Step._

  def isSuccess(code: Int) = code >= 200 && code < 300

  case class Scenario(description: String, step: Step.FreeStep[Any]) 
  
  def playerPost(name: String, psw: String, url: SiteMap => Path): Step.FreeStep[Player] = for {
    siteMap     <- getSiteMap
    playerResp1 <- post(url(siteMap), s"""{ "name":"${name}", "password":"${psw}" }""")
    _           <- check(isSuccess(playerResp1.getStatusCode), "response is ok")
    player      <- parse[Player](playerResp1.getResponseBody)(privatePlayerDecode)
    _           <- check(player.name == name, "player name must match")
  } yield player

  def playerPostFails(name: String, psw: String, url: SiteMap => Path, desc: String): Step.FreeStep[Response] = for {
    siteMap <- getSiteMap
    playerResp1 <- post(url(siteMap), s"""{ "name":"${name}", "password":"${psw}" }""")
    _ <- check(!isSuccess(playerResp1.getStatusCode), s"$desc should fail but succeed")
  } yield playerResp1
  
  def playerCreateCompetion(issuer:Player, players:Seq[Player]) = for {
    siteMap <- getSiteMap
    jsonText = s"""{
      "players":[${players.map(p => s""""${p.self}"""").mkString(",")}],
      "kind":"single-match",
      "deadline":"all-players"
    }"""
    playerResp1 <- post(issuer.createCompetition, jsonText)
  } yield ()

  def createNewPlayer(name: String, psw: String): Step.FreeStep[Player] =
    playerPost(name, psw, _.players)

  def createNewPlayerFails(name: String, psw: String): Step.FreeStep[Response] =
    playerPostFails(name, psw, _.players, "create new player")

  def playerLogin(name: String, psw: String): Step.FreeStep[Player] =
    playerPost(name, psw, _.playerLogin)

  def playerLoginFails(name: String, psw: String): Step.FreeStep[Response] =
    playerPostFails(name, psw, _.playerLogin, "player login")

  val testScenarios = Seq(
    Scenario("a player can register", for {
      _ <- createNewPlayer("pippo", "password")
    } yield ()),
  
    Scenario("cant create a player with duplicate name", for {
      _ <- createNewPlayer("pippo", "password")
      _ <- createNewPlayerFails("pippo", "password")
    } yield ()),
  
    Scenario("a player can register and logon", for {
      player <- createNewPlayer("pippo", "password")
      logPlayer <- playerLogin(player.name, "password")
    } yield ()),
  
    Scenario("a player with invalid credentials cant logon", for {
      player <- createNewPlayer("pippo", "password")
      logPlayer <- playerLoginFails(player.name, "wrong password")
    } yield ()),
  
    Scenario("a player that does not exists cant logon", for {
      logPlayer <- playerLoginFails("pippo", "password")
    } yield ())
  )  

  val decs = new TestDecoders {}
  val cfg = TestInterpreterConfig(10, "http://localhost:8080/test/site-map", decs.siteMapDecode)
  val runner = new InterpreterJettyRunner( 
      new TestInterpreter(cfg),
      () => JettyServerFactory.createServer( TestAppJettyConfig.jettyConfig(plans => Seq(plans.webApp.siteMapPlan, plans.webApp.playersPlan) ) ))
  
  testScenarios.foreach { scenario =>
    println("-"*120)
    println(scenario.description)
    println("-"*120)
    println( runner.run(scenario.step).mkString("\n") )
  }
  
  
}


