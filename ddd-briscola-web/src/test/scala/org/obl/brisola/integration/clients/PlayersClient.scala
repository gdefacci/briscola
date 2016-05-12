package org.obl.brisola.integration.clients

import org.obl.brisola.webtest.TestDecoders
import org.obl.raz.Path
import org.obl.briscola.presentation.SiteMap
import org.obl.briscola.presentation.Player
import org.obl.free.Step
import org.obl.briscola.presentation.CompetitionState
import argonaut.DecodeJson

trait PlayersClient[S] { self: SiteMapClient[S] with TestDecoders with StepFactoryHolder[S] =>

  import stepFactory._

  def playerPost(name: String, psw: String, url: SiteMap => Path, desc: String): stepFactory.FreeStep[Player] = for {
    siteMap <- getSiteMap
    playerResp1 <- post(url(siteMap), s"""{ "name":"${name}", "password":"${psw}" }""")
    _ <- check(playerResp1.is2xx, s"$desc is sucessfull")
    player <- parse[Player](playerResp1.body)(privatePlayerDecode)
    _ <- check(player.name == name, s"the returned name is ${player.name}")
  } yield player

  def playerPostFails(name: String, psw: String, url: SiteMap => Path, errorSubject: String): stepFactory.FreeStep[Step.Response] = for {
    siteMap <- getSiteMap
    playerResp1 <- post(url(siteMap), s"""{ "name":"${name}", "password":"${psw}" }""")
    _ <- check(!playerResp1.is2xx, s"$errorSubject fails")
  } yield playerResp1

  def createNewPlayer(name: String, psw: String): stepFactory.FreeStep[Player] =
    playerPost(name, psw, _.players, s"create a new player with name $name and password $psw")

  def createNewPlayerFails(name: String, psw: String): stepFactory.FreeStep[Step.Response] =
    playerPostFails(name, psw, _.players, s"create a new player with name $name and password $psw")

  def playerLogin(name: String, psw: String): stepFactory.FreeStep[Player] =
    playerPost(name, psw, _.playerLogin, s"player $name login with password $psw")

  def playerLoginFails(name: String, psw: String): stepFactory.FreeStep[Step.Response] =
    playerPostFails(name, psw, _.playerLogin, s"player $name login with password $psw")

  implicit class PlayerWebClient(player: Player) {

    def acceptCompetition(cs: CompetitionState) = for {
      _ <- check(cs.accept.isDefined, s"${player.name} is enabled to accept the competition")
      resp <- post(cs.accept.get)
      _ <- check(resp.is2xx, s"${player.name} could accept the competion")
    } yield resp

    def declineCompetition(cs: CompetitionState) = for {
      _ <- check(cs.decline.isDefined, s"${player.name} is enabled to decline the competition")
      resp <- post(cs.decline.get)
      _ <- check(resp.is2xx, s"${player.name} cuuld decline the competition")
    } yield resp

    object events {
      def collectFirstOf[T](implicit dj: DecodeJson[T]): stepFactory.FreeStep[Option[T]] = for {
        webSocketPlayer <- webSocket(player.webSocket)
      } yield webSocketPlayer.messages.collectFirst(decodePF[T])

      def getFirstOf[T](eventName: String)(implicit dj: DecodeJson[T]): stepFactory.FreeStep[T] = for {
        msg <- collectFirstOf[T]
        _ <- check(msg.isDefined, s"${player.name} events contains a $eventName event")
      } yield msg.get

      def allOf[T](implicit dj: DecodeJson[T]) = {
        for {
          webSocketPlayer <- webSocket(player.webSocket)
        } yield webSocketPlayer.messages.collect(decodePF[T])
      }
    }

    def createCompetion(players: Seq[Player]) = for {
      jsonText <- stepFactory.pure(s"""{
        "players":[${players.map(p => s""""${p.self.render}"""").mkString(",")}],
        "kind":"single-match",
        "deadline":"all-players"
      }""")
      playerResp1 <- post(player.createCompetition, jsonText)
    } yield playerResp1

  }

}
