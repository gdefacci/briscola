package com.github.gdefacci.briscola.presentation.sitemap

import org.http4s.HttpService
import org.http4s.server._
import org.http4s.dsl._
import org.obl.raz.http4s.RazHttp4s._
import com.github.gdefacci.briscola.web.util.ServletPlan
import com.github.gdefacci.briscola.presentation
import com.github.gdefacci.briscola.presentation.CommonJsonEncoders.siteMapEncoder

class SiteMapPlan(val servletPath: org.obl.raz.Path, _routes: => SiteMapRoutes, siteMap: => SiteMap) extends ServletPlan {
  
  lazy val routes = _routes
  
  import com.github.gdefacci.briscola.web.util.ArgonautEncodeHelper._

  lazy val plan = HttpService {
    case GET -> routes.SiteMap(_) => Ok( asJson(siteMap) )    
  }
  
}