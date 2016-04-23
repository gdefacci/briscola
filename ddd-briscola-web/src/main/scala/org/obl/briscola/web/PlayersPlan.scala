package org.obl.briscola
package web

import org.http4s.HttpService
import org.http4s.server._
import org.http4s.dsl._
import scalaz.{ -\/, \/, \/- }
import org.obl.raz.PathCodec
import org.obl.briscola.player.PlayerId
import org.obl.briscola.player.Player
import org.obl.briscola.presentation.EventAndState
import org.http4s.server.websocket.WS
import org.http4s.websocket.WebsocketBits
import scalaz.stream.Process
import scalaz.stream.Exchange
import org.http4s.websocket.WebsocketBits.WebSocketFrame
import org.obl.raz.PathConverter
import org.obl.raz.PathPosition
import org.obl.raz.UriTemplate
import argonaut.EncodeJson
import org.obl.briscola.competition.ClientCompetitionState
import org.obl.briscola.competition.ClientCompetitionEvent
import org.obl.briscola.service._
import org.obl.briscola.service.player._
import org.obl.raz.PathDecoder
import org.obl.briscola.web.util.ServletPlan

//trait PlayerWebSocketRoutes extends WebSocketRoutes {
//  def PlayerById: PathConverter[PlayerId, PlayerId, String, SegmentPosition, SegmentPosition]
//  def playerByIdUriTemplate: UriTemplate
//}

//trait PlayerRoutes extends ServletRoutes {
//  def Players: BiPath
//  def PlayerLogin: BiPath
//  def PlayerById: PathCodec.Symmetric[PlayerId]
//}

trait PlayerPresentationAdapter {
  def routes: PlayerRoutes
  def playerWebSocketRoutes: PlayerWebSocketRoutes
  def competitionRoutes: CompetitionRoutes

  def apply(pls: Iterable[Player]): Iterable[presentation.Player] =
    pls.map(apply(_))

  def apply(pl: Player) = presentation.Player(routes.PlayerById.encode(pl.id), pl.name,
    playerWebSocketRoutes.PlayerById.encode(pl.id),
    competitionRoutes.CreateCompetition.encode(pl.id))

  def apply(pe: player.PlayerEvent): presentation.PlayerEvent = pe match {
    case player.PlayerLogOn(pid) => presentation.PlayerLogOn(routes.PlayerById.encode(pid))
    case player.PlayerLogOff(pid) => presentation.PlayerLogOff(routes.PlayerById.encode(pid))
  }
}

object PlayerPresentationAdapter {
  def apply(proutes: => PlayerRoutes, wsRoutes: => PlayerWebSocketRoutes, pcompetitionRoutes: => CompetitionRoutes) = {
    new PlayerPresentationAdapter {
      lazy val routes = proutes
      lazy val competitionRoutes = pcompetitionRoutes
      lazy val playerWebSocketRoutes: PlayerWebSocketRoutes = wsRoutes
    }
  }
}

class PlayersPlan(val servletPath: org.obl.raz.Path, _routes: => PlayerRoutes, service: => PlayerService,
    toPresentation: => PlayerPresentationAdapter, gameToPresentation: => GamePresentationAdapter, competitionToPresentation: => CompetitionPresentationAdapter) extends ServletPlan {

  import org.obl.raz.http4s.RazHttp4s._
  import org.obl.briscola.web.util.ArgonautEncodeHelper._
  import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper._

  import jsonEncoders._
  import jsonDecoders._

  lazy val routes = _routes

  lazy val plan = HttpService {
    case GET -> Root / "hello" =>
      Ok("Hello world.")

    case GET -> routes.Players(_) =>
      val content = presentation.Collection(service.allPlayers.map(toPresentation(_)))
      Ok(asJson(content))

    case req @ POST -> routes.Players(_) =>
      ParseBody[presentation.Input.Player](req) { errOrPlayer =>
        errOrPlayer match {
          case -\/(err) => BadRequest(err)
          case \/-(pl) =>
            service.createPlayer(pl.name, pl.password) match {
              case -\/(err) => InternalServerError(err.toString)
              case \/-(content) => {
                implicit val playerEncoder = jsonEncoders.privatePlayerEncoder
                Ok(asJson(toPresentation(content)))
              }
            }

        }
      }

    case req @ POST -> routes.PlayerLogin(_) =>
      ParseBody[presentation.Input.Player](req) { errOrPlayer =>
        errOrPlayer match {
          case -\/(err) => BadRequest(err)
          case \/-(pl) =>
            service.logon(pl.name, pl.password) match {
              case -\/(err) => InternalServerError(err.toString)
              case \/-(content) => {
                implicit val playerEncoder = jsonEncoders.privatePlayerEncoder
                Ok(asJson(toPresentation(content)))
              }
            }

        }
      }

    case GET -> routes.PlayerById(id) =>
      service.playerById(id).map((p: Player) => toPresentation(p)) match {
        case Some(content) => Ok(asJson(content))
        case None => NotFound()
      }

    //    case GET -> routes.PlayerWebSocket(pid) => /* this is actuallly unused */
    //      val src = scalaz.stream.async.unboundedQueue[WebSocketFrame]
    //      val output = scalaz.stream.async.unboundedQueue[WebSocketFrame]
    //
    //      service.playersChannels(pid).map { pch =>
    //        pch.competions.collect {
    //          case (e:ClientCompetitionEvent, s:ClientCompetitionState) => e -> s
    //        }.subscribe { (evComp) =>
    //          val (event, state) = evComp
    //          val resp = WebsocketBits.Text(asJson(EventAndState(competitionToPresentation(state.id, event, pid), competitionToPresentation(state, Some(pid)))))
    //          src.enqueueOne(resp).run
    //        }
    //        pch.games.subscribe { (evGm) =>
    //          val (event, state) = evGm
    //          val id = state match {
    //            case EmptyGameState => None
    //            case gm:ActiveGameState => Some(gm.id)
    //            case gm:FinalGameState => Some(gm.id)
    //          }
    //          id.foreach { id =>
    //            val resp = WebsocketBits.Text(asJson(EventAndState(gameToPresentation(id, event, pid), gameToPresentation(state, Some(pid)))))
    //            src.enqueueOne(resp).run                
    //          }
    //        }
    //        pch.players.subscribe { (evPl) =>
    //          val (event, state) = evPl
    //          val resp = WebsocketBits.Text(asJson(EventAndState(toPresentation(event), toPresentation(state))))
    //          src.enqueueOne(resp).run
    //        }
    //        WS(Exchange(src.dequeue, output.enqueue))
    //      }.getOrElse(NotFound())

  }

}