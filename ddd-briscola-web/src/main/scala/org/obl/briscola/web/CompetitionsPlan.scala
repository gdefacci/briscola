package org.obl.briscola
package web

import org.obl.raz.PathCodec
import org.obl.briscola.player.PlayerId
import org.http4s._
import org.http4s.dsl._
import org.http4s.server._
import org.obl.briscola.competition._
import org.obl.briscola.service._
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola.web.util.ServletRoutes
import jsonEncoders._
import jsonDecoders._
import org.obl.briscola.web.util.Plan
import org.obl.briscola.player.GamePlayers

trait CompetitionRoutes extends ServletRoutes {
  def Competitions: org.obl.raz.Path
  def CompetitionById: PathCodec.Symmetric[CompetitionId]
  def PlayerCompetitionById: PathCodec.Symmetric[(CompetitionId, PlayerId)]
  def AcceptCompetition: PathCodec.Symmetric[(CompetitionId, PlayerId)]
  def DeclineCompetition: PathCodec.Symmetric[(CompetitionId, PlayerId)]
  def CreateCompetition: PathCodec.Symmetric[PlayerId]
}

object CompetitionPresentationAdapter {
  def apply(pr: => PlayerRoutes, cr: => CompetitionRoutes) = {
    new CompetitionPresentationAdapter {
      lazy val playerRoutes = pr
      lazy val competitionRoutes = cr
    }
  }

}
trait CompetitionPresentationAdapter {

  def playerRoutes: PlayerRoutes
  def competitionRoutes: CompetitionRoutes

  def apply(comp: CompetitionStartDeadline): Presentation.CompetitionStartDeadline = {
    comp match {
      case CompetitionStartDeadline.AllPlayers => Presentation.AllPlayers
      case CompetitionStartDeadline.OnPlayerCount(n) => Presentation.OnPlayerCount(n)
    } 
  }
  def apply(comp: MatchKind): Presentation.MatchKind = {
    comp match {
      case SingleMatch => Presentation.SingleMatch
      case NumberOfGamesMatchKind(n) => Presentation.NumberOfGamesMatchKind(n)
      case TargetPointsMatchKind(n) => Presentation.TargetPointsMatchKind(n)
    }
  }
  def apply(comp: Competition, pid: Option[PlayerId]): Presentation.Competition = {
    Presentation.Competition(
      GamePlayers.getPlayers(comp.players).map(p => playerRoutes.PlayerById(p)),
      apply(comp.kind),
      apply(comp.deadline))
  }
  
  def apply(cid:CompetitionId, comp: ClientCompetitionEvent, pid:PlayerId): Presentation.CompetitionEvent = comp match {
    case CreatedCompetition(id, issuer, comp) => 
      Presentation.CreatedCompetition( playerRoutes.PlayerById(issuer.id), competitionRoutes.PlayerCompetitionById(id, pid) )
      
    case CompetitionAccepted(pid) => 
      Presentation.CompetitionAccepted( playerRoutes.PlayerById(pid), competitionRoutes.PlayerCompetitionById(cid, pid) )
      
    case CompetitionDeclined(pid, rsn) => 
      Presentation.CompetitionDeclined( playerRoutes.PlayerById(pid), competitionRoutes.PlayerCompetitionById(cid, pid), rsn )
    
  }
  
  def apply(comp: ClientCompetitionState, pid: Option[PlayerId]): Presentation.CompetitionState = {
    val (competition, compKind, acceptingPlayers, decliningPlayers) = comp match {
      case c: OpenCompetition => (Some(c.competition), Presentation.CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
      case c: DroppedCompetition => (Some(c.competition), Presentation.CompetitionStateKind.dropped, c.acceptingPlayers, c.decliningPlayers)
      case c: FullfilledCompetition => (Some(c.competition), Presentation.CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
    }

    Presentation.CompetitionState(
      competitionRoutes.CompetitionById(comp.id),
      competition.map(apply(_, pid)),
      compKind,
      acceptingPlayers.map(id => playerRoutes.PlayerById(id)).toSet,
      decliningPlayers.map(id => playerRoutes.PlayerById(id)).toSet,
      for (plid <- pid) yield (competitionRoutes.AcceptCompetition(comp.id, plid)),
      for (plid <- pid) yield (competitionRoutes.DeclineCompetition(comp.id, plid)))
  }

}

class CompetitionsPlan(_routes: => CompetitionRoutes, _playerRoutes: => PlayerRoutes, service: => CompetitionsService, toPresentation: => CompetitionPresentationAdapter)  extends PlayerRoutesJsonDecoders with Plan {

  lazy val routes = _routes
  lazy val playerRoutes = _playerRoutes
  
  import org.obl.raz.http4s.RazHttp4s._
  import org.obl.briscola.web.util.ArgonautEncodeHelper._
  import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper._

  lazy val onlyClientCompetitionState:PartialFunction[CompetitionState, ClientCompetitionState] = {
    case cc:ClientCompetitionState => cc
  }
  
  lazy val plan = HttpService {
    case GET -> routes.Competitions(_) =>
      val content = Presentation.Collection(service.allCompetitions.collect(onlyClientCompetitionState).map(toPresentation(_, None)))
      Ok(responseBody(content))

    case GET -> routes.CompetitionById(id) =>
      val content = service.competitionById(id).collect(onlyClientCompetitionState).map(toPresentation(_, None))
      content match {
        case None => NotFound()
        case Some(content) => Ok(responseBody(content))
      }
    case GET -> routes.PlayerCompetitionById(id, pid) =>
      val content = service.competitionById(id).collect(onlyClientCompetitionState).map(toPresentation(_, Some(pid)))
      content match {
        case None => NotFound()
        case Some(content) => Ok(responseBody(content))
      }

    case POST -> routes.AcceptCompetition(id, pid) =>
      service.acceptCompetition(pid, id).map {
        case -\/(err) => InternalServerError(err.toString)
        case \/-(st) =>
          println(st)
          st match {
            case v:ClientCompetitionState =>
              val content = toPresentation(v, Some(pid))
              Ok(responseBody(content))
              
            case _ => NotFound()
          }
      } getOrElse (NotFound())

    case POST -> routes.DeclineCompetition(id, pid) =>
      service.declineCompetition(pid, id, None).map {
        case -\/(err) => InternalServerError(err.toString)
        case \/-(v:ClientCompetitionState) =>
          val content = toPresentation(v, Some(pid))
          Ok(responseBody(content))
        case _ => NotFound()
      } getOrElse (NotFound())

      
    case req @ POST -> routes.CreateCompetition(pid) =>
      ParseBody[Presentation.Input.Competition](req) { errOrComp => 
        errOrComp match {
          case -\/(err) => InternalServerError(err.toString)
          case \/-(comp) => 
            service.createCompetition(pid, org.obl.briscola.player.Players(comp.players.toSet), comp.kind, comp.deadline) match {
              case -\/(err) => InternalServerError(err.toString)
              case \/-(content:ClientCompetitionState) => Ok(responseBody( toPresentation(content, Some(pid))))
              case _ => NotFound()
            }
        }
      } 
  }

}