package com.github.gdefacci.briscola.presentation.competition

import com.github.gdefacci.briscola.presentation
import org.http4s._
import org.http4s.dsl._
import com.github.gdefacci.briscola.competition.{CompetitionError}
import com.github.gdefacci.briscola.service._
import scalaz.{ -\/, \/, \/- }
import presentation.competition.CompetitionJsonEncoders._
import presentation.competition.CompetitionJsonDecoders._
import com.github.gdefacci.briscola.web.util.ServletPlan
import com.github.gdefacci.briscola.web.util.ToPresentation
import com.github.gdefacci.briscola.web.util.PresentationAdapter
import com.github.gdefacci.briscola.service.competition.CompetitionsService
import com.github.gdefacci.briscola.presentation.player.PlayersInputAdapter
import com.github.gdefacci.briscola.competition
import com.github.gdefacci.briscola.presentation.PlayerCompetitionState

class CompetitionsPlan(val servletPath: org.obl.raz.Path, routes: => CompetitionRoutes, service: => CompetitionsService, 
    toModel: => PlayersInputAdapter, toPresentation:ToPresentation[CompetitionError])(
    implicit competitionStateAdapter:PresentationAdapter[competition.ClientCompetitionState, CompetitionState],    
    		playerCompetitionStateAdapter:PresentationAdapter[PlayerCompetitionState, CompetitionState]    
    ) extends ServletPlan {

  import org.obl.raz.http4s.RazHttp4s._
  import com.github.gdefacci.briscola.web.util.ArgonautHttp4sDecodeHelper._

  import scalaz.std.list._
  import scalaz.std.option
  
  lazy val onlyClientCompetitionState: PartialFunction[competition.CompetitionState, competition.ClientCompetitionState] = {
    case cc: competition.ClientCompetitionState => cc
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
      ParseBody[presentation.competition.Input.Competition](req) { errOrComp =>
        errOrComp match {
          case -\/(err) => BadRequest(err.toString)
          case \/-(comp) =>
            toModel(comp.players).map { gamePlayers =>
              toPresentation(service.createCompetition(pid, gamePlayers, comp.kind, comp.deadline).map {
                case content: competition.ClientCompetitionState => Some(content)
                case _ => None
              })
            }.getOrElse(BadRequest())
        }
      }
  }

}