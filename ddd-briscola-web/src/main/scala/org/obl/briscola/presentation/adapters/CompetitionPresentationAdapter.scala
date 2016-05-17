package org.obl.briscola.presentation.adapters

import org.obl.briscola.web.PlayerRoutes
import org.obl.briscola.web.CompetitionRoutes
import org.obl.briscola.competition._
import org.obl.briscola.presentation
import org.obl.briscola.player.PlayerId
import org.obl.briscola.player.GamePlayers
import org.obl.briscola.web.util.PresentationAdapter
import org.obl.briscola.service.player.PlayerCompetitionEvent
import org.obl.briscola.service.player.PlayerCompetitionState

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

  lazy val competitionStartDeadlineAdapter = PresentationAdapter((comp: CompetitionStartDeadline) =>
    comp match {
      case CompetitionStartDeadline.AllPlayers => presentation.AllPlayers
      case CompetitionStartDeadline.OnPlayerCount(n) => presentation.OnPlayerCount(n)
    })

  lazy val matchKindAdapter = PresentationAdapter((comp: MatchKind) =>
    comp match {
      case SingleMatch => presentation.SingleMatch
      case NumberOfGamesMatchKind(n) => presentation.NumberOfGamesMatchKind(n)
      case TargetPointsMatchKind(n) => presentation.TargetPointsMatchKind(n)
    })

  private def toPresentation(comp: Competition, pid: Option[PlayerId]): presentation.Competition = {
    presentation.Competition(
      GamePlayers.getPlayers(comp.players).map(p => playerRoutes.PlayerById.encode(p)),
      matchKindAdapter(comp.kind),
      competitionStartDeadlineAdapter(comp.deadline))
  }

  implicit lazy val playerCompetionEventAdapter = PresentationAdapter[PlayerCompetitionEvent, presentation.CompetitionEvent]((compEv: PlayerCompetitionEvent) => compEv.event match {
    case CreatedCompetition(id, issuer, comp) =>
      presentation.CreatedCompetition(playerRoutes.PlayerById.encode(issuer.id), competitionRoutes.PlayerCompetitionById.encode(compEv.competitionId, compEv.playerId))

    case CompetitionAccepted(pid) =>
      presentation.CompetitionAccepted(playerRoutes.PlayerById.encode(pid), competitionRoutes.PlayerCompetitionById.encode(compEv.competitionId, compEv.playerId))

    case CompetitionDeclined(pid, rsn) =>
      presentation.CompetitionDeclined(playerRoutes.PlayerById.encode(pid), competitionRoutes.PlayerCompetitionById.encode(compEv.competitionId, compEv.playerId), rsn)

  })

  private def toPresentation(competitionState: ClientCompetitionState, pid: Option[PlayerId]): presentation.CompetitionState = {
    val (competition, compKind, acceptingPlayers, decliningPlayers) = competitionState match {
      case c: OpenCompetition => (c.competition, presentation.CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
      case c: DroppedCompetition => (c.competition, presentation.CompetitionStateKind.dropped, c.acceptingPlayers, c.decliningPlayers)
      case c: FullfilledCompetition => (c.competition, presentation.CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
    }
    val compId = competitionState.id

    presentation.CompetitionState(
      competitionRoutes.CompetitionById.encode(compId),
      Some(toPresentation(competition, pid)),
      compKind,
      acceptingPlayers.map(id => playerRoutes.PlayerById.encode(id)).toSet,
      decliningPlayers.map(id => playerRoutes.PlayerById.encode(id)).toSet,
      pid.map(competitionRoutes.AcceptCompetition.encode(compId, _)),
      pid.map(competitionRoutes.DeclineCompetition.encode(compId, _)))
  }

  implicit lazy val clientCompetitionStateAdapter = PresentationAdapter((compEv: ClientCompetitionState) => {
    toPresentation(compEv, None)
  })

  implicit lazy val playerCompetitionStateAdapter = PresentationAdapter((compEv: PlayerCompetitionState) => {
    toPresentation(compEv.competitionState, Some(compEv.playerId))
    //    val compId = compEv.competitionState.id
    //    
    //    val (competition, compKind, acceptingPlayers, decliningPlayers) = compEv.competitionState match {
    //      case c: OpenCompetition => (Some(c.competition), presentation.CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
    //      case c: DroppedCompetition => (Some(c.competition), presentation.CompetitionStateKind.dropped, c.acceptingPlayers, c.decliningPlayers)
    //      case c: FullfilledCompetition => (Some(c.competition), presentation.CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
    //    }
    //    
    //    presentation.CompetitionState(
    //      competitionRoutes.CompetitionById.encode(compId),
    //      competition.map(toPresentation(_, pid)),
    //      compKind,
    //      acceptingPlayers.map(id => playerRoutes.PlayerById.encode(id)).toSet,
    //      decliningPlayers.map(id => playerRoutes.PlayerById.encode(id)).toSet,
    //      pid.map(competitionRoutes.AcceptCompetition.encode(compId, _)),
    //      pid.map(competitionRoutes.DeclineCompetition.encode(compId, _)) )
  })

}