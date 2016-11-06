package com.github.gdefacci.briscola.presentation.competition

import org.obl.raz.Api.PathMatchDecoder
import org.obl.raz.Api.PathCodec
import com.github.gdefacci.briscola.competition.CompetitionId
import com.github.gdefacci.briscola.player.PlayerId

trait CompetitionRoutes {
  def Competitions: PathMatchDecoder
  def CompetitionById: PathCodec.Symmetric[CompetitionId]
  def PlayerCompetitionById: PathCodec.Symmetric[(CompetitionId, PlayerId)]
  def AcceptCompetition: PathCodec.Symmetric[(CompetitionId, PlayerId)]
  def DeclineCompetition: PathCodec.Symmetric[(CompetitionId, PlayerId)]
  def CreateCompetition: PathCodec.Symmetric[PlayerId]
}
