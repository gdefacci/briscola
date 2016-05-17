package org.obl.briscola.webtest

import org.scalatest.FunSuite
import scalaz.{ \/- }
import org.obl.briscola.player.PlayerId
import org.obl.briscola.GameId
import org.obl.briscola.competition.CompetitionId
import org.obl.raz._
import org.obl.briscola.integration.TestAppConfig

class AppRoutesTest extends FunSuite {

  val appRoutes = TestAppConfig.appRoutes

  val player1 = PlayerId(1)
  val game1 = GameId(2)
  val competition1 = CompetitionId(3)

  test("playerWebSocketRoutes") {

    val rts = appRoutes.playerWebSocketRoutes

    assert(rts.PlayerById.encode(player1).render == "ws://localhost:8080/test/ws/players/1")

    assert(rts.PlayerById.decodeFull(Path / "test" / "ws" / "players" / "1") == \/-(player1))

    assert("/ws/players/{playerId}" == appRoutes.playerWebSocketRoutes.playerByIdUriTemplate.render)

  }

  test("players / byId") {

    val rts = appRoutes.playerRoutes

    assert(rts.PlayerById.encode(player1).render == "http://localhost:8080/test/players/1")

    assert(rts.PlayerById.decodeFull(Path / "1") == \/-(player1))
    
    val p1 = Path(Some(HTTP), Some(Authority("localhost",8080)), List("test", "players", "1"), Nil, None)
    
    val dec1 = (HTTP("localhost",8080) / "test" / "players" / PathConverter.Segment.string).pathConverter

    assert(dec1.decodeFull(p1) == \/-("1"))
    
    val r1 = rts.PlayerById.fullPath.decodeFull(p1)
    assert(rts.PlayerById.fullPath.decodeFull(p1) == \/-(player1))

  }

  test("playerLogin") {

    val rts = appRoutes.playerRoutes

    assert(rts.PlayerLogin.path.render == "http://localhost:8080/test/players/login")

    assert(rts.PlayerLogin.decodeFull(Path / "login") == \/-(Path / "login"))
  }

  test("players") {

    val rts = appRoutes.playerRoutes

    assert(rts.Players.path.render == "http://localhost:8080/test/players")

    assert(rts.Players.decodeFull(Path) == \/-(Path))
  }

  test("games / byId") {

    val rts = appRoutes.gameRoutes

    assert(rts.GameById.encode(game1).render == "http://localhost:8080/test/games/2")

    assert(rts.GameById.decodeFull(Path / "2") == \/-(game1))
  }

  test("games") {

    val rts = appRoutes.gameRoutes

    assert(rts.Games.path.render == "http://localhost:8080/test/games")

    assert(rts.Games.decodeFull(Path) == \/-(Path))
  }

  test("games / player") {

    val rts = appRoutes.gameRoutes

    assert(rts.Player.encode(game1, player1).render == "http://localhost:8080/test/games/2/player/1")

    assert(rts.Player.decodeFull(Path / "2" / "player" / "1") == \/-(game1 -> player1))
  }

  test("games / team") {

    val rts = appRoutes.gameRoutes

    assert(rts.Team.encode(game1, "team-a").render == "http://localhost:8080/test/games/2/team/team-a")

    assert(rts.Team.decodeFull(Path / "2" / "team" / "team-a") == \/-(game1 -> "team-a"))
  }

  test("competitions") {

    val rts = appRoutes.competitionRoutes

    assert(rts.Competitions.path.render == "http://localhost:8080/test/competitions")

    assert(rts.Competitions.decodeFull(Path) == \/-(Path))
  }

  test("competitons / accept") {

    val rts = appRoutes.competitionRoutes

    assert(rts.AcceptCompetition.encode(competition1, player1).render == "http://localhost:8080/test/competitions/3/player/1/accept")

    assert(rts.AcceptCompetition.decodeFull(Path / "3" / "player" / "1" / "accept") == \/-(competition1 -> player1))
  }
  
  test("competitons / decline") {

    val rts = appRoutes.competitionRoutes

    assert(rts.DeclineCompetition.encode(competition1, player1).render == "http://localhost:8080/test/competitions/3/player/1/decline")

    assert(rts.DeclineCompetition.decodeFull(Path / "3" / "player" / "1" / "decline") == \/-(competition1 -> player1))
  }
  
  test("competitons / create") {

    val rts = appRoutes.competitionRoutes

    assert(rts.CreateCompetition.encode(player1).render == "http://localhost:8080/test/competitions/player/1")

    assert(rts.CreateCompetition.decodeFull(Path / "player" / "1") == \/-(player1))
  }

  test("competitons / byId") {

    val rts = appRoutes.competitionRoutes

    assert(rts.CompetitionById.encode(competition1).render == "http://localhost:8080/test/competitions/3")

    assert(rts.CompetitionById.decodeFull(Path / "3") == \/-(competition1))
  }
  
 test("competitons / playerCompetition") {

    val rts = appRoutes.competitionRoutes

    assert(rts.PlayerCompetitionById.encode(competition1, player1).render == "http://localhost:8080/test/competitions/3/player/1")

    assert(rts.PlayerCompetitionById.decodeFull(Path / "3" / "player" / "1") == \/-(competition1 -> player1))
  }

}