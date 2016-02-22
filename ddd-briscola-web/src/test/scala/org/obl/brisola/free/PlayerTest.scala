package org.obl.brisola.free

import org.obl.briscola.presentation.CompetitionState
import org.obl.briscola.presentation.CreatedCompetition
import org.obl.briscola.presentation.EventAndState
import org.obl.briscola.presentation.Player
import org.obl.briscola.presentation.SiteMap
import org.obl.brisola.webtest.TestDecoders
import org.obl.raz.Path

import com.ning.http.client.Response

trait SiteMapClient { self: TestDecoders =>
  import Step._

  protected def siteMapUrl: String

  def getSiteMap = for {
    resp <- get(siteMapUrl)
    siteMap <- parse[SiteMap](resp.getResponseBody)
  } yield siteMap

}

trait PlayerClient { self:SiteMapClient with TestDecoders with HttpTestHelper =>
  
  import Step._

  def playerPost(name: String, psw: String, url: SiteMap => Path): Step.FreeStep[Player] = for {
    siteMap <- getSiteMap
    playerResp1 <- post(url(siteMap), s"""{ "name":"${name}", "password":"${psw}" }""")
    _ <- check(isSuccess(playerResp1.getStatusCode), "response is sucessfull")
    player <- parse[Player](playerResp1.getResponseBody)(privatePlayerDecode)
    _ <- check(player.name == name, "player name match")
  } yield player

  def playerPostFails(name: String, psw: String, url: SiteMap => Path, errorSubject: String): Step.FreeStep[Response] = for {
    siteMap <- getSiteMap
    playerResp1 <- post(url(siteMap), s"""{ "name":"${name}", "password":"${psw}" }""")
    _ <- check(!isSuccess(playerResp1.getStatusCode), s"$errorSubject fails")
  } yield playerResp1

  def playerCreateCompetion(issuer: Player, players: Seq[Player]) = for {
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
  
}

object PlayerTest extends ScenariosRunner with 
        JettyApplicationTestRunner with 
        TestDecoders with
        HttpTestHelper with RxTestHelper with
        SiteMapClient with PlayerClient with 
        App {

  lazy val siteMapUrl = "http://localhost:8080/test/site-map"
  lazy val testInterpreterConfig = TestInterpreterConfig(3)
  
  import Step._

  import CompetionEventDecoders._  
    
  lazy val scenarios = Seq(
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
    } yield ()),

    Scenario("a logged player can start a competion", for {
      player1 <- createNewPlayer("pippo", "password")
      player2 <- createNewPlayer("pluto", "pass")
      webSocketPlayer2 <- webSocket(player2.webSocket)
      comp <- playerCreateCompetion(issuer = player1, Seq(player2))
      _ <- check(
          contains[EventAndState[CreatedCompetition, CompetitionState]](
              webSocketPlayer2, c => c.event.issuer == player1.self), 
              "contains competion started event")
    } yield ()))

  ConsoleTestReporter(testResults)  
    
}




