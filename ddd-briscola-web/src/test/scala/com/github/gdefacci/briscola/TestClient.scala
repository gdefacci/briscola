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
import com.github.gdefacci.briscola.presentation.EventAndState
import com.github.gdefacci.briscola.presentation.competition.CreatedCompetition
import com.github.gdefacci.briscola.presentation.competition.CompetitionState
import argonaut.DecodeJson
import scalaz.Unapply
import scalaz.Applicative

trait TestClient {

  import ClientStepFactory._
  import TestDecoders.PrivatePlayer._
  import TestDecoders.decodePF

  def createPlayer(siteMap: SiteMap, name: String, password: String): ClientStep.Free[Player] = for {
    resp <- post(siteMap.players, s"""{ "name":"$name", "password":"$password" }""")
    _ <- check(if (resp.is2xx) Ok else Fail(s"could no create player $name cause ${resp.body}"))
    r <- parse[Player](resp.body)
  } yield r

  def createPlayers(siteMap: SiteMap, players: Seq[String]): ClientStep.Free[Seq[(String, Player)]] = {

    import scalaz._
    import Scalaz._

    for {
      resps <- players.map(pl => createPlayer(siteMap, pl, "password")).toList.sequenceU
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

  def playerPlaysCard(playerState: Path, card: PlayerState => Card) = {
    import TestDecoders.{ playerStateDecode }

    for {
      ps <- getAndParse[PlayerState](playerState, "player state")
      c = card(ps)
      resp <- post(playerState, s"""{ "number": ${c.number}, "seed":"${c.seed.toString}"  }""")
    } yield resp
  }

  def playerAcceptTheCompetition(webSocketPath: Path): ClientStep.Free[TestResult[String]] = {
    import TestDecoders.{ playerStateDecode, decodePF, competitionStateDecode, stateAndEventDecoder }
    import TestDecoders.CompetitionEventDecoders._

    for {
      msgs <- webSocket(webSocketPath)
      comp = msgs.messages.collectFirst(decodePF[EventAndState[CreatedCompetition, CompetitionState]]).get
      resp <- post(comp.state.accept.get)
    } yield if (resp.is2xx) Ok else Fail(s"cant accept competition ${resp.body}")
  }

  def playerDeclineTheCompetition(webSocketPath: Path): ClientStep.Free[TestResult[String]] = {
    import TestDecoders.{ playerStateDecode, decodePF, competitionStateDecode, stateAndEventDecoder }
    import TestDecoders.CompetitionEventDecoders._

    for {
      msgs <- webSocket(webSocketPath)
      comp = msgs.messages.collectFirst(decodePF[EventAndState[CreatedCompetition, CompetitionState]]).get
      resp <- post(comp.state.decline.get)
    } yield if (resp.is2xx) Ok else Fail(s"cant decline competition ${resp.body}")
  }

  def getAndParse[T](pth: Path, desc: String)(implicit dj: DecodeJson[T]) = for {
    resp <- get(pth)
    _ <- check(if (resp.is2xx) Ok else Fail(s"error fetching $desc at ${pth.render}"))
    res <- parse[T](resp.body)
  } yield res

  def messagesOf[T](webSocketPath: Path)(implicit dj: DecodeJson[T]) = {
    import scalaz._
    import Scalaz._

    for {
      ch <- webSocket(webSocketPath)
    } yield ch.messages.collect(decodePF[T]).toList
  }

}