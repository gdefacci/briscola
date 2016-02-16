package org.obl.brisola.free

import org.obl.briscola.web.RoutesServletConfig
import org.obl.briscola.web.WebAppConfig.DefaultRoutesConfig
import org.obl.raz._
import org.obl.briscola.web.BriscolaWebAppConfig
import org.obl.briscola.web.util.ServletPlan
import org.obl.briscola.web.util.JettyWebAppConfig

object TestAppJettyConfig {
  
  lazy val testContextPath = "test"
  lazy val port = 8080
  
  lazy val testServletConfig = new RoutesServletConfig with DefaultRoutesConfig {
    val host = HTTP("localhost", port)
    val contextPath = PathSg(testContextPath)
  }
  
  def testPlans(plans:BriscolaWebAppConfig => Seq[ServletPlan]): Seq[ServletPlan] = {
    val webAppConfig:BriscolaWebAppConfig = new BriscolaWebAppConfig(testServletConfig, org.obl.briscola.service.Config.createSimpleApp)
    plans(webAppConfig)
  }

  def jettyConfig(plans:BriscolaWebAppConfig => Seq[ServletPlan]) = JettyWebAppConfig(port, testContextPath, testPlans(plans))
}