package org.obl.briscola.web.plans

import org.http4s.HttpService
import org.http4s.server._
import org.http4s.dsl._
import org.obl.raz.http4s.RazHttp4s._
import org.obl.briscola.web.util.ServletPlan
import org.obl.briscola.presentation
import org.obl.briscola.web.SiteMapRoutes
import org.obl.briscola.web.jsonEncoders

class SiteMapPlan(val servletPath: org.obl.raz.Path, _routes: => SiteMapRoutes, siteMap: => presentation.SiteMap) extends ServletPlan {
  
  lazy val routes = _routes
  
  import org.obl.briscola.web.util.ArgonautEncodeHelper._

  import jsonEncoders.siteMapEncoder

  lazy val plan = HttpService {
    case GET -> routes.SiteMap(_) => Ok( asJson(siteMap) )    
  }
  
}