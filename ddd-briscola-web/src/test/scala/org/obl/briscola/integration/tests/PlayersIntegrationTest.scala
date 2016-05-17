package org.obl.briscola.integration
package tests

import org.obl.briscola.web.BriscolaWebApp
import org.obl.briscola.web.util.ServletContextPlanAdder
import org.obl.briscola.integration.BriscolaIntegrationTest
import org.obl.briscola.integration.clients.{PlayersClient, SiteMapClient, StepFactoryHolder}
import org.obl.briscola.webtest.TestDecoders

trait PlayersIntegrationTest[S] extends BriscolaIntegrationTest[S] with StepFactoryHolder[S] with TestDecoders with SiteMapClient[S] with PlayersClient[S] {

  lazy val siteMapUrl = TestAppConfig.siteMapUrl

  protected def mountPlans(context: ServletContextPlanAdder, webApp: BriscolaWebApp) = {
	  webApp.SiteMap.plans.foreach( context.addPlan(_) )
    webApp.Players.plans.foreach( context.addPlan(_) )
    webApp.Competitions.plans.foreach( context.addPlan(_) )
  }

} 