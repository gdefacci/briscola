package com.github.gdefacci.briscola.presentation.game

import org.http4s.HttpService
import org.http4s.dsl._
import scalaz.{ -\/, \/, \/- }
import com.github.gdefacci.briscola.web.util.ServletPlan
import com.github.gdefacci.briscola.web.util.ToPresentation
import com.github.gdefacci.briscola.web.util.PresentationAdapter
import com.github.gdefacci.briscola.service.game.GameService
import com.github.gdefacci.briscola.game

class GamesPlan(
    val servletPath: org.obl.raz.Path, 
    routes: => GameRoutes, service: => GameService, 
    toPresentation: => ToPresentation[game.BriscolaError])(
    implicit gameStatePresentationAdapter:PresentationAdapter[game.GameState, GameState],
    playerStatePresentationAdapter:PresentationAdapter[game.PlayerState, PlayerState],
    finalPlayerStatePresentationAdapter:PresentationAdapter[game.PlayerFinalState, PlayerFinalState]) extends ServletPlan {

  import org.obl.raz.http4s.RazHttp4s._
  import com.github.gdefacci.briscola.web.util.ArgonautHttp4sDecodeHelper._

  import GameJsonEncoders._
  import GameJsonDecoders._

  import scalaz.std.list._
  import scalaz.std.option._
  
  lazy val plan = HttpService {

    case GET -> routes.Games(_) =>
        toPresentation(\/-( Some(service.allGames.toList )))
      
    case GET -> routes.GameById(id) =>
      toPresentation(\/-(service.gameById(id)))

    case req @ POST -> routes.Player(id, pid) =>
      ParseBody[game.Card](req) { errOrcard =>
        errOrcard match {
          case -\/(err) => BadRequest(err.toString)
          case \/-(card) =>
            toPresentation(service.playCard(id, pid, card))
        }
      }

    case GET -> routes.Player(gid, pid) =>
      service.gameById(gid).map { gm =>
        gm match {
          case gm: game.ActiveGameState => toPresentation( \/-(gm.players.find(p => p.id == pid) ))
          case gm: game.FinalGameState => toPresentation( \/-(gm.players.find(p => p.id == pid)))
          case _ => NotFound()
        }
      } getOrElse NotFound()

  }

}