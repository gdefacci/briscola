package com.github.gdefacci.briscola.web
package modules

import org.obl.raz.{Path, Authority}

object ConfModule {
  
  val host = Authority("localhost", 8080)
  val contextPath = Path / "app"
  
}