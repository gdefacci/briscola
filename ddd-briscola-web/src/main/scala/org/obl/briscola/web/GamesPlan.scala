package org.obl.briscola
package web

import org.http4s.HttpService
import org.http4s.server._
import org.http4s.dsl._
import org.obl.raz.PathCodec
import org.obl.briscola.player.PlayerId
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola.service._
import org.obl.briscola.web.util.ServletPlan

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

  def apply(card:Card):presentation.Card = 
    presentation.Card(card.number, card.seed, card.points)
  
  def apply(ps: PlayerState): presentation.PlayerState =
    presentation.PlayerState(playerRoutes.PlayerById.encode(ps.id), ps.cards.map(apply(_)), presentation.Score(ps.score.cards.map( c => apply(c))) )

  def apply(ps: PlayerFinalState): presentation.PlayerFinalState =
    presentation.PlayerFinalState(playerRoutes.PlayerById.encode(ps.id), ps.points, presentation.Score(ps.score.cards.map(apply(_))) )

  def apply(ps: PlayerLeft): presentation.PlayerLeft =
    presentation.PlayerLeft(playerRoutes.PlayerById.encode(ps.player), ps.reason)
  
  def apply(ps: DropReason): presentation.DropReason = ps match {
    case pl:PlayerLeft => apply(pl)
  }
    
    
  def apply(gid:GameId, e: BriscolaEvent, pid:PlayerId): presentation.BriscolaEvent = e match {
    case GameStarted(gm) => presentation.GameStarted(apply(gm, Some(pid)))
    case GameDropped(dropReason) => presentation.GameDropped(gameRoutes.GameById.encode(gid), apply(dropReason) )
    case CardPlayed(pid, crd) => presentation.CardPlayed(
      gameRoutes.GameById.encode(gid),
      playerRoutes.PlayerById.encode(pid),
      apply(crd))
  }

  def apply(gm: ActiveGameState, player: Option[PlayerId]): presentation.ActiveGameState =
    presentation.ActiveGameState(
      gameRoutes.GameById.encode(gm.id),
      apply(gm.briscolaCard), 
      gm.teams.map(tms => tms.teams.map( t => gameRoutes.Team.encode(gm.id, t.name))),
      gm.moves.map(m => presentation.Move(playerRoutes.PlayerById.encode(m.player.id), apply(m.card))),
      gm.nextPlayers.map(p => playerRoutes.PlayerById.encode(p.id)),
      playerRoutes.PlayerById.encode(gm.currentPlayer.id),
      gm.isLastHandTurn, gm.isLastGameTurn,
      gm.players.map(p => playerRoutes.PlayerById.encode(p.id)),
      player.map(pid => gameRoutes.Player.encode(gm.id, pid)),
      gm.deckCardsNumber)

  def apply(ts:TeamScore):presentation.TeamScore = {
    presentation.TeamScore(
        ts.team.name, 
        ts.team.players.map(pid => playerRoutes.PlayerById.encode(pid)), 
        ts.score.cards.map(apply(_)), 
        ts.score.points )
  }    
      
      
  def apply(gm: DroppedGameState): presentation.DroppedGameState =
    presentation.DroppedGameState(
      gameRoutes.GameById.encode(gm.id),
      apply(gm.briscolaCard), 
      gm.teams.map(tms => tms.teams.map( t => gameRoutes.Team.encode(gm.id, t.name))),
      gm.moves.map(m => presentation.Move(playerRoutes.PlayerById.encode(m.player.id), apply(m.card))),
      gm.nextPlayers.map(p => playerRoutes.PlayerById.encode(p.id)),
      apply(gm.dropReason))
  
  def apply(gm: FinalGameState): presentation.FinalGameState = {
    val teamResult = for (teamOrderByPoint <- gm.teamScoresOrderByPoints; winnerTeam <- gm.winnerTeam) yield(
        presentation.TeamsGameResult(teamOrderByPoint.map(apply(_)), apply(winnerTeam))
    )
    
    val gmr = teamResult.getOrElse(presentation.PlayersGameResult(gm.playersOrderByPoints.map(apply(_)), apply(gm.winner)))
    
    presentation.FinalGameState(
        gameRoutes.GameById.encode(gm.id),
        apply(gm.briscolaCard), gmr)
  }
      
  def apply(gm: GameState): presentation.GameState = apply(gm, None)

  def apply(gm: GameState, player: Option[PlayerId]): presentation.GameState = gm match {
    case EmptyGameState => presentation.EmptyGameState
    case gm: ActiveGameState => apply(gm, player)
    case gm: DroppedGameState => apply(gm)
    case gm: FinalGameState => apply(gm)
  }
}

class GamesPlan(val servletPath: org.obl.raz.Path, _routes: => GameRoutes, service: => BriscolaService, toPresentation: => GamePresentationAdapter) extends ServletPlan {

  import org.obl.raz.http4s.RazHttp4s._
  import org.obl.briscola.web.util.ArgonautEncodeHelper._
  import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper._

  import jsonEncoders._
  import jsonDecoders._
  lazy val routes = _routes

  lazy val plan = HttpService {

    case GET -> routes.Games(_) =>
      val content = presentation.Collection( service.allGames.map(toPresentation(_)) )
      Ok(asJson(content))

    case GET -> routes.GameById(id) =>
      val content = service.gameById(id).map(toPresentation(_))
      content match {
        case None => NotFound()
        case Some(content) => Ok(asJson(content))
      }

    case req @ POST -> routes.Player(id, pid) =>
      ParseBody[Card](req) { errOrcard =>
        errOrcard match {
          case -\/(err) => BadRequest(err.toString)
          case \/-(card) =>
            service.playCard(id, pid, card) match {
              case None => NotFound()
              case Some(-\/(err)) => InternalServerError(err.toString)
              case Some(\/-(content)) => Ok(asJson(toPresentation(content, Some(pid))))
            }
        }
      }

    case GET -> routes.Player(gid, pid) =>
      service.gameById(gid).flatMap { gm =>
        gm match {
          case gm: ActiveGameState => gm.players.find(p => p.id == pid).map(toPresentation(_)).map(content => Ok(asJson(content)))
          case gm: FinalGameState => gm.players.find(p => p.id == pid).map(toPresentation(_)).map(content => Ok(asJson(content)))
          case _ => None
        }
      } getOrElse NotFound()
  }

}