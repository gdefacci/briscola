package org.obl.briscola.web

import org.obl.raz._

object WebAppConfig {

  trait DefaultRoutesConfig {
    val playerServletPath = PathSg("players")
    val gameServletPath = PathSg("games")
    val competitionServletPath = PathSg("competitions")
    val siteMapServletPath = PathSg("site-map")

  }

  val development = new RoutesServletConfig with DefaultRoutesConfig {
    val host = HTTP("localhost", 8080)
    val contextPath = PathSg("app")
  }

}