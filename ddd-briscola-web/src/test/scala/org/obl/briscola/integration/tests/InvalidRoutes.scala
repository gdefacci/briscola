package org.obl.briscola.integration.tests

import org.scalatest.FunSuite
import org.obl.free._
import org.obl.briscola.web.AppRoutes
import org.obl.briscola.player.PlayerId

class InvalidRoutes extends FunSuite with PlayersIntegrationTest[AppRoutes] with ScalaTestReporter {

  import stepFactory._

  lazy val background = Backgrounds.givenApplicationRoutes

  lazy val scenarios = Seq(
    Scenario("asking for a player that does not exists yields a 404", for {
      routes <- initialState
      url = routes.playerRoutes.PlayerById.encode(PlayerId(88888))
//      resp1 <- Describe(s"get a non exisiting player at $url"), get(url))
      resp1 <- get(url)
      _ <- check(resp1.code == 404, "status code is 404")
    } yield ()))

  verify(testResults)

}