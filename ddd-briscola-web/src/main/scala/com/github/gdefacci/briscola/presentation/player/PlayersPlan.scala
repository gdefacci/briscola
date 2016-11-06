package com.github.gdefacci.briscola.presentation.player

import org.http4s.HttpService
import org.http4s.dsl._
import scalaz.{ -\/, \/, \/- }
import com.github.gdefacci.briscola.service.player.PlayerService
import com.github.gdefacci.briscola.web.util.ServletPlan
import com.github.gdefacci.briscola.web.util.ToPresentation
import com.github.gdefacci.briscola.web.util.PresentationAdapter
import com.github.gdefacci.briscola.player.PlayerError
import com.github.gdefacci.briscola.player.{Player => DomainPlayer}


class PlayersPlan(val servletPath: org.obl.raz.Path, routes: => PlayerRoutes, service: => PlayerService, toPresentation:ToPresentation[PlayerError])(
    implicit playerPresentationAdapter: PresentationAdapter[DomainPlayer, Player]) extends ServletPlan {
  
  import org.obl.raz.http4s.RazHttp4s._
  import com.github.gdefacci.briscola.web.util.ArgonautHttp4sDecodeHelper._

  import com.github.gdefacci.briscola.presentation.player.PlayerJsonDecoders._
  import com.github.gdefacci.briscola.presentation.player.PlayerJsonEncoders._

  import scalaz.std.list._
  import scalaz.std.option
  
  lazy val plan = HttpService {
    case GET -> Root / "hello" =>
      Ok("Hello world.")

    case GET -> routes.Players(_) =>
      toPresentation(\/-(Some(service.loggedPlayers.toList)))

    case req @ POST -> routes.Players(_) =>
      ParseBody[Input.Player](req) { errOrPlayer =>
        errOrPlayer match {
          case -\/(err) => BadRequest(err)
          case \/-(pl) =>
            implicit val playerEncoder = PlayerJsonEncoders.privatePlayerEncoder

            toPresentation(service.createPlayer(pl.name, pl.password).map(Some(_)))

        }
      }

    case req @ POST -> routes.PlayerLogin(_) =>
      ParseBody[Input.Player](req) { errOrPlayer =>
        errOrPlayer match {
          case -\/(err) => BadRequest(err)
          case \/-(pl) =>
            implicit val playerEncoder = PlayerJsonEncoders.privatePlayerEncoder

            toPresentation(service.logon(pl.name, pl.password).map(Some(_)))

        }
      }

    case GET -> routes.PlayerById(id) =>
      toPresentation(\/-(service.playerById(id)))

  }

}