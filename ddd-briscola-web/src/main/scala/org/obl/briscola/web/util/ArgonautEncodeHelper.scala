package org.obl.briscola.web.util

import argonaut.{EncodeJson, PrettyParams}

object ArgonautEncodeHelper {
  
  def asJson[T](t:T)(implicit enc:EncodeJson[T]):String = {
    PrettyParams.spaces2.pretty(enc(t))
  }
  
}