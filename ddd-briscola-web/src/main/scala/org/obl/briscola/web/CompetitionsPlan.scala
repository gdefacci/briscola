package org.obl.briscola
package web

import org.obl.raz.PathCodec
import org.obl.briscola.player.PlayerId
import org.http4s._
import org.http4s.dsl._
import org.http4s.server._
import org.obl.briscola.competition._
import scalaz.{ -\/, \/, \/- }

trait CompetitionRoutes {
  def Competitions: org.obl.raz.Path
  def CompetitionById: PathCodec.Symmetric[CompetitionId]
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

  def apply(comp: Competition, pid: Option[PlayerId]): Presentation.Competition = {
    Presentation.Competition(
      comp.players.map(p => playerRoutes.PlayerById(p.id)),
      comp.kind,
      comp.deadlineKind)
  }
  
  def apply(comp: CompetitionEvent): Presentation.CompetitionEvent = comp match {
    case CreatedCompetition(issuer, comp) => 
      Presentation.CreatedCompetition( playerRoutes.PlayerById(issuer.id), competitionRoutes.CompetitionById(comp.id) )
      
    case ConfirmedCompetition(comp) => 
      Presentation.ConfirmedCompetition( competitionRoutes.CompetitionById(comp.id) )
      
    case CompetitionAccepted(pid, cid) => 
      Presentation.CompetitionAccepted( playerRoutes.PlayerById(pid), competitionRoutes.CompetitionById(cid) )
      
    case CompetitionDeclined(pid, cid, rsn) => 
      Presentation.CompetitionDeclined( playerRoutes.PlayerById(pid), competitionRoutes.CompetitionById(cid), rsn )
    
  }
  
  def apply(comp: CompetitionState, pid: Option[PlayerId]): Presentation.CompetitionState = {
    val (competition, compKind, acceptingPlayers, decliningPlayers) = comp match {
      case EmptyCompetition => (None, Presentation.CompetitionStateKind.empty, Nil, Nil)
      case c: OpenCompetition => (Some(c.competition), Presentation.CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
      case c: DroppedCompetition => (Some(c.competition), Presentation.CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
      case c: FullfilledCompetition => (Some(c.competition), Presentation.CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
    }

    val compId = competition.map(_.id)

    Presentation.CompetitionState(
      compId.map(competitionRoutes.CompetitionById(_)),
      competition.map(apply(_, pid)),
      compKind,
      acceptingPlayers.map(id => playerRoutes.PlayerById(id)).toSet,
      decliningPlayers.map(id => playerRoutes.PlayerById(id)).toSet,
      for (plid <- pid; cid <- compId) yield (competitionRoutes.AcceptCompetition(cid, plid)),
      for (plid <- pid; cid <- compId) yield (competitionRoutes.DeclineCompetition(cid, plid)))
  }

}

import jsonEncoders._
import jsonDecoders._

class CompetitionsPlan(routes: => CompetitionRoutes, _playerRoutes: => PlayerRoutes, service: => CompetitionsService, toPresentation: => CompetitionPresentationAdapter)  extends PlayerRoutesJsonDecoders {

  lazy val playerRoutes = _playerRoutes
  
  import org.obl.raz.http4s.RazHttp4s._
  import org.obl.briscola.web.util.ArgonautEncodeHelper._
  import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper._

  lazy val plan = HttpService {
    case GET -> routes.Competitions(_) =>
      val content = service.allCompetitions.map(toPresentation(_, None))
      Ok(responseBody(content))

    case GET -> routes.CompetitionById(id) =>
      val content = service.competitionById(id).map(toPresentation(_, None))
      content match {
        case None => NotFound()
        case Some(content) => Ok(responseBody(content))
      }

    case POST -> routes.AcceptCompetition(id, pid) =>
      service.acceptCompetition(pid, id).map {
        case -\/(err) => InternalServerError(err.toString)
        case \/-(v) =>
          val content = toPresentation(v, Some(pid))
          Ok(responseBody(content))

      } getOrElse (NotFound())

    case POST -> routes.DeclineCompetition(id, pid) =>
      service.declineCompetition(pid, id, None).map {
        case -\/(err) => InternalServerError(err.toString)
        case \/-(v) =>
          val content = toPresentation(v, Some(pid))
          Ok(responseBody(content))

      } getOrElse (NotFound())

      
    case req @ POST -> routes.CreateCompetition(pid) =>
      ParseBody[Presentation.Input.Competition](req) { errOrComp => 
        errOrComp match {
          case -\/(err) => InternalServerError(err.toString)
          case \/-(comp) => 
            service.createCompetition(pid, comp.players, comp.kind, comp.deadlineKind) match {
              case -\/(err) => InternalServerError(err.toString)
              case \/-(content) => Ok(responseBody( toPresentation(content, Some(pid)))) 
            }
        }
      } 
  }

}