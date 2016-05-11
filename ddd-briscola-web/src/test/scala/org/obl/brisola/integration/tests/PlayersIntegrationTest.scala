package org.obl.brisola.integration
package tests

import org.obl.briscola.web.BriscolaWebApp
import org.obl.briscola.web.util.ServletContextPlanAdder
import org.obl.brisola.integration.BriscolaIntegrationTest
import org.obl.brisola.integration.clients.{PlayersClient, SiteMapClient, StepFactoryHolder}
import org.obl.brisola.webtest.TestDecoders


trait PlayersIntegrationTest[S] extends BriscolaIntegrationTest[S] with StepFactoryHolder[S] with TestDecoders with SiteMapClient[S] with PlayersClient[S] {

  lazy val siteMapUrl = TestAppConfig.siteMapUrl

  protected def mountPlans(context: ServletContextPlanAdder, webApp: BriscolaWebApp) = {
    context.addPlan(webApp.competitionsPlan)
    context.addPlan(webApp.playersPlan)
    context.addPlan(webApp.siteMapPlan)
  }

} 