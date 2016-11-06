package com.github.gdefacci.briscola.web.util

import org.http4s.server._
import org.obl.raz.PathCodec
import org.obl.raz.PathPosition
import org.obl.raz.PathConverter
import org.obl.raz.Path
import org.obl.raz.PathDecoder
import org.obl.raz.PathEncoder
import org.obl.raz.UriTemplateEncoder
import org.http4s.HttpService

trait Plan {

  def plan: HttpService

}

trait ServletPlan extends Plan {
  
  def servletPath: Path
  
}