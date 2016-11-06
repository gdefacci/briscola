package com.github.gdefacci.briscola

import com.github.gdefacci.free.ClientStepFactory
import com.github.gdefacci.briscola.presentation.sitemap.SiteMap
import com.github.gdefacci.bdd.Ok
import com.github.gdefacci.bdd.Fail
import com.github.gdefacci.briscola.presentation.player.Player
import com.github.gdefacci.free.ClientStep
import com.github.gdefacci.bdd.TestResult
import org.obl.raz.Path
import com.github.gdefacci.briscola.presentation.game.PlayerState
import com.github.gdefacci.briscola.game.Card

trait TestClient {

  import ClientStepFactory._
  import TestDecoders.PrivatePlayer._

  def createPlayer(siteMap: SiteMap, name: String): ClientStep.Free[ClientStep.Response] = for {
    r <- post(siteMap.players, s"""{ "name":"${name}", "password":"password" }""")
    _ <- check(if (r.is2xx) Ok else Fail(s"could no create player $name cause ${r.body}"))
  } yield r

  def createPlayers(siteMap: SiteMap, players: Seq[String]): ClientStep.Free[Seq[(String, Player)]] = {

    import scalaz._
    import Scalaz._

    for {
      playerResps <- players.map(pl => createPlayer(siteMap, pl)).toList.sequenceU
      resps <- playerResps.map(resp => parse[Player](resp.body)).sequence
    } yield {
      players.zip(resps)
    }
  }

  def playerReceivedMessage(player: Player, predicate: String => Boolean): ClientStep.Free[TestResult[String]] =
    for {
      msg <- webSocket(player.webSocket)
    } yield if (msg.messages.exists(predicate)) Ok else Fail(s"can find '$predicate'")

  def playersReceivedMessage(players: Seq[Player], predicate: String => Boolean): ClientStep.Free[List[TestResult[String]]] = {
    import scalaz._
    import Scalaz._

    for {
      msgs <- players.toList.map(player => webSocket(player.webSocket)).sequenceU
    } yield msgs.map(msg => if (msg.messages.exists(predicate)) Ok else Fail(s"can find '$predicate'"))

  }

  def playerStartsCompetition(player: Player, otherPlayers: Seq[Player], kind: String, deadLine: String): ClientStep.Free[ClientStep.Response] =
    for {
      resp <- post(player.createCompetition, s"""{
          "players":[${otherPlayers.map(p => s""""${p.self.render}"""").mkString(",")}],
          "kind": $kind,
          "deadline":$deadLine
        }""")
      _ <- check(if (resp.is2xx) Ok else Fail(s"cant create competition ${resp.body}"))
    } yield resp

  import TestDecoders.{ playerStateDecode }

  def playerPlaysCard(playerState: Path, card: PlayerState => Card) = (for {
    psResp <- get(playerState)
    ps <- parse[PlayerState](psResp.body)
    c = card(ps)
    resp <- post(playerState, s"""{ "number": ${c.number}, "seed":"${c.seed.toString}"  }""")
  } yield resp)

}