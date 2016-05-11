package org.obl.brisola.integration.clients

import org.obl.brisola.webtest.TestDecoders
import org.obl.briscola.presentation.SiteMap

trait SiteMapClient[S] { self: TestDecoders with StepFactoryHolder[S] =>

  import stepFactory._

  protected def siteMapUrl: String

  def getSiteMap = for {
    resp <- get(siteMapUrl)
    siteMap <- parse[SiteMap](resp.body)
  } yield siteMap

}

