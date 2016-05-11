package org.obl.brisola.integration

import org.obl.briscola.web.RoutesServletConfig
import org.obl.raz._
import org.obl.briscola.web.BriscolaWebApp

object TestAppConfig {
  
  lazy val authority = Authority("localhost", 8080)
  lazy val contextPath = Path / "test"
  lazy val port = 8080
  
  lazy val resources = new org.obl.briscola.web.Resources(authority, contextPath, RoutesServletConfig)
  lazy val appRoutes = new org.obl.briscola.web.AppRoutes(resources)
  
  def simpleWebApp = new BriscolaWebApp(appRoutes, org.obl.briscola.service.Config.createSimpleApp)
  
  lazy val siteMapUrl = appRoutes.siteMapRoutes.SiteMap.path.render
}