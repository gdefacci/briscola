package org.obl.briscola
package web

import org.http4s.HttpService
import org.http4s.dsl._
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola.service.BriscolaService
import org.obl.briscola.web.util.ServletPlan


class GamesPlan(val servletPath: org.obl.raz.Path, _routes: => GameRoutes, service: => BriscolaService, toPresentation: => ToPresentation)(
    implicit gameStatePresentationAdapter:PresentationAdapter[GameState, presentation.GameState],
    playerStatePresentationAdapter:PresentationAdapter[PlayerState, presentation.PlayerState],
    finalPlayerStatePresentationAdapter:PresentationAdapter[PlayerFinalState, presentation.PlayerFinalState]) extends ServletPlan {

  import org.obl.raz.http4s.RazHttp4s._
  import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper._

  import jsonEncoders._
  import jsonDecoders._
  lazy val routes = _routes

  lazy val plan = HttpService {

    case GET -> routes.Games(_) =>
        toPresentation(\/-( Some(service.allGames.toList )))
      
    case GET -> routes.GameById(id) =>
      toPresentation(\/-(service.gameById(id)))

    case req @ POST -> routes.Player(id, pid) =>
      ParseBody[Card](req) { errOrcard =>
        errOrcard match {
          case -\/(err) => BadRequest(err.toString)
          case \/-(card) =>
            toPresentation(service.playCard(id, pid, card))
        }
      }

    case GET -> routes.Player(gid, pid) =>
      service.gameById(gid).map { gm =>
        gm match {
          case gm: ActiveGameState => toPresentation( \/-(gm.players.find(p => p.id == pid) ))
          case gm: FinalGameState => toPresentation( \/-(gm.players.find(p => p.id == pid)))
          case _ => NotFound()
        }
      } getOrElse NotFound()

  }

}