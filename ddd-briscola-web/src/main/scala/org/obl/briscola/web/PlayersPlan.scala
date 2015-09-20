package org.obl.briscola
package web

import org.http4s.server._
import org.http4s.dsl._
import scalaz.{-\/, \/, \/-}
import org.obl.raz.PathCodec
import org.obl.briscola.player.PlayerId
import org.obl.briscola.web.Presentation.EventAndState
import org.http4s.server.websocket.WS
import org.http4s.websocket.WebsocketBits
import scalaz.stream.Process
import scalaz.stream.Exchange
import org.obl.briscola.web.util.RazWsHelper


trait PlayerRoutes {
	def Players:org.obl.raz.Path
  def PlayerById:PathCodec.Symmetric[PlayerId]
	def PlayerWebSocket:PathCodec.Symmetric[PlayerId]
}

trait PlayerPresentationAdapter {
  def routes:PlayerRoutes
  def competitionRoutes:CompetitionRoutes
  
  def apply(pl:player.Player) = Presentation.Player(routes.PlayerById(pl.id), pl.name, 
      RazWsHelper.asWebSocket(routes.PlayerWebSocket(pl.id)),
      competitionRoutes.CreateCompetition(pl.id)) 
}

object PlayerPresentationAdapter {
  def apply(proutes: => PlayerRoutes, pcompetitionRoutes: => CompetitionRoutes) = {
    new PlayerPresentationAdapter {
      lazy val routes = proutes
      lazy val competitionRoutes = pcompetitionRoutes
    }
  }
}

class PlayersPlan(routes: => PlayerRoutes, service: => PlayerService, 
    toPresentation: => PlayerPresentationAdapter, gameToPresentation: => GamePresentationAdapter, competitionToPresentation: => CompetitionPresentationAdapter) {

  import org.obl.raz.http4s.RazHttp4s._
  import org.obl.briscola.web.util.ArgonautEncodeHelper._
  import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper._
  
  import jsonEncoders._
  import jsonDecoders._

  lazy val plan = HttpService {
    case GET -> Root / "hello" =>
      Ok("Hello world.")
      
    case GET -> routes.Players(_) =>
      val content = service.allPlayers.map(toPresentation(_))
      Ok(responseBody( content ))
   
    case req @ POST -> routes.Players(_) =>
      ParseBody[Presentation.Input.Player](req) { errOrPlayer =>
        errOrPlayer match {
          case -\/(err) => BadRequest(err)
          case \/-(pl) => 
            service.createPlayer(pl.name) match {
              case -\/(err) => InternalServerError(err.toString)
              case \/-(content) => Ok( responseBody(toPresentation(content)) )
            }
            
        }
      }
      
    case GET -> routes.PlayerById(id) =>
      service.playerById(id).map( (p:player.Player) => toPresentation(p)) match {
        case Some(content) => Ok(responseBody(content))
        case None => NotFound()
      }
        
    case GET -> routes.PlayerWebSocket(pid) =>
      val gch = service.playerGamesChannel(pid).map { proc =>
        proc.map { (evSt) =>
          val (event, state) = evSt
          WebsocketBits.Text( responseBody(EventAndState(gameToPresentation(event), gameToPresentation(state, Some(pid)))) )
        }
      }
      val cch = service.playerCompetitionsChannel(pid).map { proc =>
        proc.map { (evSt) =>
          val (event, state) = evSt
          WebsocketBits.Text( responseBody(EventAndState(competitionToPresentation(event), competitionToPresentation(state, Some(pid)))) )
        }
      }
      val src = (gch, cch) match {
        case (None, None) => Process.halt
        case (Some(ch), None) => ch
        case (None, Some(ch)) => ch
        case (Some(ch1), Some(ch2)) => ch1 merge ch2
      }
      WS(Exchange(src, Process.halt ))
  }
  
}