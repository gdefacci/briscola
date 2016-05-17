package org.obl.briscola
package web
package plans

import org.obl.briscola.presentation
import org.http4s._
import org.http4s.dsl._
import org.obl.briscola.competition._
import org.obl.briscola.service._
import scalaz.{ -\/, \/, \/- }
import jsonDecoders._
import jsonEncoders._
import org.obl.briscola.web.util.ServletPlan
import org.obl.briscola.service.player.PlayerCompetitionState
import org.obl.briscola.presentation.adapters.input.GamePlayersInputAdapter
import org.obl.briscola.web.util.ToPresentation
import org.obl.briscola.web.util.PresentationAdapter

class CompetitionsPlan(val servletPath: org.obl.raz.Path, routes: => CompetitionRoutes, service: => CompetitionsService, 
    toModel: => GamePlayersInputAdapter, toPresentation:ToPresentation)(
    implicit competitionStateAdapter:PresentationAdapter[ClientCompetitionState, presentation.CompetitionState],    
    		playerCompetitionStateAdapter:PresentationAdapter[PlayerCompetitionState, presentation.CompetitionState]    
    ) extends ServletPlan {

  import org.obl.raz.http4s.RazHttp4s._
  import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper._

  import scalaz.std.list._
  import scalaz.std.option
  
  lazy val onlyClientCompetitionState: PartialFunction[CompetitionState, ClientCompetitionState] = {
    case cc: ClientCompetitionState => cc
  }

  lazy val plan = HttpService {
    case GET -> routes.Competitions(_) =>
      toPresentation(\/-(Some(service.allCompetitions.collect(onlyClientCompetitionState).toList)))

    case GET -> routes.CompetitionById(id) =>
      toPresentation(\/-(service.competitionById(id).collect(onlyClientCompetitionState)))
      
    case GET -> routes.PlayerCompetitionById(id, pid) =>
      toPresentation(\/-(service.competitionById(id).collect(onlyClientCompetitionState).map(PlayerCompetitionState(pid,_))))

    case POST -> routes.AcceptCompetition(id, pid) =>
      toPresentation( service.acceptCompetition(pid, id).map { resOpt =>
        resOpt.collect(onlyClientCompetitionState).map(PlayerCompetitionState(pid,_))
      } )
      
    case POST -> routes.DeclineCompetition(id, pid) =>
      toPresentation( service.declineCompetition(pid, id, None).map { resOpt =>
        resOpt.collect(onlyClientCompetitionState).map(PlayerCompetitionState(pid,_))
      } )
      
    case req @ POST -> routes.CreateCompetition(pid) =>
      ParseBody[presentation.Input.Competition](req) { errOrComp =>
        errOrComp match {
          case -\/(err) => BadRequest(err.toString)
          case \/-(comp) =>
            toModel(comp.players).map { gamePlayers =>
              toPresentation(service.createCompetition(pid, gamePlayers, comp.kind, comp.deadline).map {
                case content: ClientCompetitionState => Some(content)
                case _ => None
              })
            }.getOrElse(BadRequest())
        }
      }
  }

}