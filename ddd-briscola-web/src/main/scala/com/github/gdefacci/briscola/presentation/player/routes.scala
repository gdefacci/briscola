package com.github.gdefacci.briscola.presentation.player

import org.obl.raz.Api.PathMatchDecoder
import org.obl.raz.Api.PathCodec
import com.github.gdefacci.briscola.player.PlayerId
import org.obl.raz.UriTemplate

trait PlayerRoutes {
  def Players: PathMatchDecoder
  def PlayerLogin: PathMatchDecoder
  def PlayerById: PathCodec.Symmetric[PlayerId]
}

trait PlayerWebSocketRoutes {
  def PlayerById: PathCodec.Symmetric[PlayerId]
  def playerByIdUriTemplate: UriTemplate
}