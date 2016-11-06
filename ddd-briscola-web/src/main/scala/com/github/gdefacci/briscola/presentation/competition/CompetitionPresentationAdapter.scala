package com.github.gdefacci.briscola.presentation.competition

import com.github.gdefacci.briscola.presentation.player.PlayerRoutes
import com.github.gdefacci.briscola.{competition => model}
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.player.GamePlayers
import com.github.gdefacci.briscola.web.util.PresentationAdapter
import com.github.gdefacci.briscola.presentation.PlayerCompetitionEvent
import com.github.gdefacci.briscola.presentation.PlayerCompetitionState

class CompetitionPresentationAdapter(playerRoutes: PlayerRoutes, competitionRoutes: CompetitionRoutes) {

  lazy val competitionStartDeadlineAdapter = PresentationAdapter((comp: model.CompetitionStartDeadline) =>
    comp match {
      case model.CompetitionStartDeadline.AllPlayers => AllPlayers
      case model.CompetitionStartDeadline.OnPlayerCount(n) => OnPlayerCount(n)
    })

  lazy val matchKindAdapter = PresentationAdapter((comp: model.MatchKind) =>
    comp match {
      case model.SingleMatch => SingleMatch
      case model.NumberOfGamesMatchKind(n) => NumberOfGamesMatchKind(n)
      case model.TargetPointsMatchKind(n) => TargetPointsMatchKind(n)
    })

  private def toPresentation(comp: model.Competition, pid: Option[PlayerId]): Competition = {
    Competition(
      GamePlayers.getPlayers(comp.players).map(p => playerRoutes.PlayerById.encode(p)),
      matchKindAdapter(comp.kind),
      competitionStartDeadlineAdapter(comp.deadline))
  }

  implicit lazy val playerCompetionEventAdapter = PresentationAdapter[PlayerCompetitionEvent, CompetitionEvent]((compEv: PlayerCompetitionEvent) => compEv.event match {
    case model.CreatedCompetition(id, issuer, comp) =>
      CreatedCompetition(playerRoutes.PlayerById.encode(issuer.id), competitionRoutes.PlayerCompetitionById.encode(compEv.competitionId, compEv.playerId))

    case model.CompetitionAccepted(pid) =>
      CompetitionAccepted(playerRoutes.PlayerById.encode(pid), competitionRoutes.PlayerCompetitionById.encode(compEv.competitionId, compEv.playerId))

    case model.CompetitionDeclined(pid, rsn) =>
      CompetitionDeclined(playerRoutes.PlayerById.encode(pid), competitionRoutes.PlayerCompetitionById.encode(compEv.competitionId, compEv.playerId), rsn)

  })

  private def toPresentation(competitionState: model.ClientCompetitionState, pid: Option[PlayerId]): CompetitionState = {
    val (competition, compKind, acceptingPlayers, decliningPlayers) = competitionState match {
      case c: model.OpenCompetition => (c.competition, CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
      case c: model.DroppedCompetition => (c.competition, CompetitionStateKind.dropped, c.acceptingPlayers, c.decliningPlayers)
      case c: model.FullfilledCompetition => (c.competition, CompetitionStateKind.open, c.acceptingPlayers, c.decliningPlayers)
    }
    val compId = competitionState.id

    CompetitionState(
      competitionRoutes.CompetitionById.encode(compId),
      Some(toPresentation(competition, pid)),
      compKind,
      acceptingPlayers.map(id => playerRoutes.PlayerById.encode(id)).toSet,
      decliningPlayers.map(id => playerRoutes.PlayerById.encode(id)).toSet,
      pid.map(competitionRoutes.AcceptCompetition.encode(compId, _)),
      pid.map(competitionRoutes.DeclineCompetition.encode(compId, _)))
  }

  implicit lazy val clientCompetitionStateAdapter = PresentationAdapter((compEv: model.ClientCompetitionState) => {
    toPresentation(compEv, None)
  })

  implicit lazy val playerCompetitionStateAdapter = PresentationAdapter((compEv: PlayerCompetitionState) => {
    toPresentation(compEv.competitionState, Some(compEv.playerId))
  })

}