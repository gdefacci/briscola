package com.github.gdefacci.briscola.presentation.player

import argonaut.DecodeJson

object PlayerJsonDecoders {
  
  implicit val playerDecoder = DecodeJson.derive[Input.Player]
  
}