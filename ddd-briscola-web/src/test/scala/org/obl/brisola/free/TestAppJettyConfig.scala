package org.obl.brisola.free

import org.obl.briscola.web.RoutesServletConfig
import org.obl.raz._
import org.obl.briscola.web.BriscolaWebAppConfig
import org.obl.briscola.web.util.ServletPlan
import org.obl.briscola.web.util.JettyWebAppConfig
import org.obl.briscola.web.AppConfigFactory

object TestAppJettyConfig {
  
  lazy val authority = Authority("localhost", 8080)
  lazy val testContextPath = Path / "test"
  lazy val port = 8080
  
  def jettyConfig(plans:BriscolaWebAppConfig => Seq[ServletPlan]) = {
    jettyConfigAndApplication(plans)._2
  }
  
  val contextPath = Path / "app"
  lazy val resources = new org.obl.briscola.web.Resources(authority, contextPath, RoutesServletConfig)
  lazy val appRoutes = new org.obl.briscola.web.AppRoutes(resources)
  lazy val simpleWebAppConfig = new BriscolaWebAppConfig(appRoutes, org.obl.briscola.service.Config.simple.app)
  
  def jettyConfigAndApplication(plans:BriscolaWebAppConfig => Seq[ServletPlan]):(BriscolaWebAppConfig, JettyWebAppConfig) = {
    val webAppConfig:BriscolaWebAppConfig = simpleWebAppConfig
    webAppConfig -> JettyWebAppConfig(port, testContextPath.render, plans(webAppConfig))
  }
}