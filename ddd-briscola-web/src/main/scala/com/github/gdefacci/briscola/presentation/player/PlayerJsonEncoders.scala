package com.github.gdefacci.briscola.presentation.player

import argonaut._
import Argonaut._
import org.obl.raz.Path

import EncodeJson.derive

import com.github.gdefacci.briscola.web.util.ArgonautHelper._

object PlayerJsonEncoders {

  import com.github.gdefacci.briscola.presentation.CommonJsonEncoders._
  
  implicit lazy val playerEncoder =
    jencode2L((pl: Player) => (pl.self, pl.name))("self", "name")

  lazy val privatePlayerEncoder = EncodeJson.derive[Player]

  implicit lazy val playerEventEncoder = {

    implicit lazy val playerEventKindEncoder = enumEncoder[PlayerEventKind.type]

    implicit lazy val playerLogOnEncoder = withKind[PlayerEventKind.type](derive[PlayerLogOn])
    implicit lazy val playerLogOffEncoder = withKind[PlayerEventKind.type](derive[PlayerLogOff])

    jencode1((p: PlayerEvent) => p match {
      case c: PlayerLogOn => playerLogOnEncoder(c)
      case c: PlayerLogOff => playerLogOffEncoder(c)
    })
  }

}