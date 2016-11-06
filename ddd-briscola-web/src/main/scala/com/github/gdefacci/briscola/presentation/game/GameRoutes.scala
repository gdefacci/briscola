package com.github.gdefacci.briscola.presentation.game

import org.obl.raz.Api.PathMatchDecoder
import org.obl.raz.Api.PathCodec
import com.github.gdefacci.briscola.game.GameId
import com.github.gdefacci.briscola.player.PlayerId

trait GameRoutes {
  def Games: PathMatchDecoder
  def GameById: PathCodec.Symmetric[GameId]
  def Player: PathCodec.Symmetric[(GameId, PlayerId)]
  def Team: PathCodec.Symmetric[(GameId, String)]
}
