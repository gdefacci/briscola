package org.obl.briscola.web.util

import argonaut.{EncodeJson, PrettyParams}

object ArgonautEncodeHelper {
  
  def responseBody[T](t:T)(implicit enc:EncodeJson[T]):String = {
    PrettyParams.spaces2.pretty(enc(t))
  }
  
}