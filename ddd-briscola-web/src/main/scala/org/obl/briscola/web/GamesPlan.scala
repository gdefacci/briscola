package org.obl.briscola
package web

import org.http4s.server._
import org.http4s.dsl._
import org.obl.raz.PathCodec
import org.obl.briscola.player.PlayerId
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola.web.util.ServletRoutes
import org.obl.briscola.web.util.Plan
import org.obl.briscola.service._

trait GameRoutes extends ServletRoutes {
  def Games: org.obl.raz.Path
  def GameById: PathCodec.Symmetric[GameId]
  def Player: PathCodec.Symmetric[(GameId, PlayerId)]
}

object GamePresentationAdapter {
  def apply(gr: => GameRoutes, pr: => PlayerRoutes) = {
    new GamePresentationAdapter {
      lazy val gameRoutes = gr
      lazy val playerRoutes = pr
    }
  }
}

trait GamePresentationAdapter {

  def gameRoutes: GameRoutes
  def playerRoutes: PlayerRoutes

  def apply(ps: PlayerState): Presentation.PlayerState =
    Presentation.PlayerState(playerRoutes.PlayerById(ps.id), ps.cards, ps.score)

  def apply(ps: PlayerFinalState): Presentation.PlayerFinalState =
    Presentation.PlayerFinalState(playerRoutes.PlayerById(ps.id), ps.points, ps.score)

  def apply(gid:GameId, e: BriscolaEvent, pid:PlayerId): Presentation.BriscolaEvent = e match {
    case GameStarted(gm) => Presentation.GameStarted(apply(gm, Some(pid)))
    case CardPlayed(pid, crd) => Presentation.CardPlayed(
      gameRoutes.GameById(gid),
      playerRoutes.PlayerById(pid),
      crd)
  }

  def apply(gm: ActiveGameState, player: Option[PlayerId]): Presentation.ActiveGameState =
    Presentation.ActiveGameState(
      gameRoutes.GameById(gm.id),
      gm.briscolaCard, gm.moves.map(m => Presentation.Move(playerRoutes.PlayerById(m.player.id), m.card)),
      gm.nextPlayers.map(p => playerRoutes.PlayerById(p.id)),
      playerRoutes.PlayerById(gm.currentPlayer.id),
      gm.isLastHandTurn, gm.isLastGameTurn,
      gm.players.map(p => playerRoutes.PlayerById(p.id)),
      player.map(pid => gameRoutes.Player(gm.id, pid)),
      gm.deckCardsNumber)

  def apply(gm: GameState): Presentation.GameState = apply(gm, None)

  def apply(gm: GameState, player: Option[PlayerId]): Presentation.GameState = gm match {
    case EmptyGameState => Presentation.EmptyGameState
    case gm: ActiveGameState => apply(gm, player)
    case gm: FinalGameState =>
      Presentation.FinalGameState(
        gameRoutes.GameById(gm.id),
        gm.briscolaCard,
        gm.playersOrderByPoints.map(apply(_)),
        apply(gm.winner))
  }
}

class GamesPlan(_routes: => GameRoutes, service: => BriscolaService, toPresentation: => GamePresentationAdapter) extends Plan {

  import org.obl.raz.http4s.RazHttp4s._
  import org.obl.briscola.web.util.ArgonautEncodeHelper._
  import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper._

  import jsonEncoders._
  import jsonDecoders._
  lazy val routes = _routes

  lazy val plan = HttpService {

    case GET -> routes.Games(_) =>
      val content = Presentation.Collection( service.allGames.map(toPresentation(_)) )
      Ok(responseBody(content))

    case GET -> routes.GameById(id) =>
      val content = service.gameById(id).map(toPresentation(_))
      content match {
        case None => NotFound()
        case Some(content) => Ok(responseBody(content))
      }

    case req @ POST -> routes.Player(id, pid) =>
      ParseBody[Card](req) { errOrcard =>
        errOrcard match {
          case -\/(err) => BadRequest(err.toString)
          case \/-(card) =>
            service.playCard(id, pid, card) match {
              case None => NotFound()
              case Some(-\/(err)) => InternalServerError(err.toString)
              case Some(\/-(content)) => Ok(responseBody(toPresentation(content, Some(pid))))
            }
        }
      }

    case GET -> routes.Player(gid, pid) =>
      service.gameById(gid).flatMap { gm =>
        gm match {
          case gm: ActiveGameState => gm.players.find(p => p.id == pid).map(toPresentation(_)).map(content => Ok(responseBody(content)))
          case gm: FinalGameState => gm.players.find(p => p.id == pid).map(toPresentation(_)).map(content => Ok(responseBody(content)))
          case _ => None
        }
      } getOrElse NotFound()
  }

}