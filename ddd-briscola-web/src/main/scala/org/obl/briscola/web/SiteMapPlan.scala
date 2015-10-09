package org.obl.briscola.web

import org.http4s.server._
import org.http4s.dsl._
import org.obl.raz.http4s.RazHttp4s._
import org.obl.briscola.web.util.Plan

class SiteMapPlan(_routes: => SiteMapRoutes, siteMap: => Presentation.SiteMap) extends Plan {
  
  lazy val routes = _routes
  
  import org.obl.briscola.web.util.ArgonautEncodeHelper._

  import jsonEncoders.siteMapEncoder

  lazy val plan = HttpService {
    case GET -> routes.SiteMap(_) => Ok( responseBody(siteMap) )    
  }
  
}