package com.github.gdefacci.briscola.presentation

import argonaut.DecodeJson

object CommonJsonDecoders {

  implicit def SeqDecodeJson[A](implicit e: DecodeJson[A]): DecodeJson[Seq[A]] = DecodeJson.CanBuildFromDecodeJson[A, Seq]

}