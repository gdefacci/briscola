package org.obl.briscola
package web
package plans

import org.http4s.HttpService
import org.http4s.dsl._
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola.player.Player
import org.obl.briscola.service.PlayerService
import org.obl.briscola.web.util.ServletPlan


class PlayersPlan(val servletPath: org.obl.raz.Path, routes: => PlayerRoutes, service: => PlayerService, toPresentation:ToPresentation)(
    implicit playerPresentationAdapter: PresentationAdapter[Player, presentation.Player]) extends ServletPlan {
  
  import org.obl.raz.http4s.RazHttp4s._
  import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper._

  import jsonEncoders._
  import jsonDecoders._

  import scalaz.std.list._
  import scalaz.std.option
  
  lazy val plan = HttpService {
    case GET -> Root / "hello" =>
      Ok("Hello world.")

    case GET -> routes.Players(_) =>
      toPresentation(\/-(Some(service.allPlayers.toList)))

    case req @ POST -> routes.Players(_) =>
      ParseBody[presentation.Input.Player](req) { errOrPlayer =>
        errOrPlayer match {
          case -\/(err) => BadRequest(err)
          case \/-(pl) =>
            implicit val playerEncoder = jsonEncoders.privatePlayerEncoder

            toPresentation(service.createPlayer(pl.name, pl.password).map(Some(_)))

        }
      }

    case req @ POST -> routes.PlayerLogin(_) =>
      ParseBody[presentation.Input.Player](req) { errOrPlayer =>
        errOrPlayer match {
          case -\/(err) => BadRequest(err)
          case \/-(pl) =>
            implicit val playerEncoder = jsonEncoders.privatePlayerEncoder

            toPresentation(service.logon(pl.name, pl.password).map(Some(_)))

        }
      }

    case GET -> routes.PlayerById(id) =>
      toPresentation(\/-(service.playerById(id)))

  }

}