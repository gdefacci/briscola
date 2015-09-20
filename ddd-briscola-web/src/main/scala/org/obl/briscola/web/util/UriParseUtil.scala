package org.obl.briscola.web.util

import java.net.URLDecoder
import org.obl.raz._
import scala.util.Try

object UriParseUtil extends App {
  private def decodeUrl(str: String) = {
    URLDecoder.decode(str.replace("+", "%2B"), "UTF-8").replace("%2B", "+").trim
  }

  def parseUrl(str: String): Option[Path] = {
    if (str.trim.length == 0) None
    else Try(new java.net.URI(decodeUrl(str))).toOption.map { uri =>
      val path = uri.getPath

      val base =
        if (uri.getScheme != null && uri.getSchemeSpecificPart != null) {
          uri.getScheme match {
            case "http" if (uri.getPort > 0) => Some(HTTP(uri.getHost, uri.getPort))            
            case "http" => Some(HTTP(uri.getHost))            
            case "https" if (uri.getPort > 0) => Some(HTTPS(uri.getHost, uri.getPort))
            case "https" => Some(HTTPS(uri.getHost))
            case _ => None
          }
        } else {
          None
        }

      val pth = if (path.startsWith("/")) path.substring(1) else path
      val pathParts = pth.split("/")

      val params: Seq[QParamSg] =
        if (uri.getQuery == null) {
          Nil
        } else {
          uri.getQuery().split(",|&").map { nmVal =>
            val nmVals: Array[String] = nmVal.split("=")
            val len = nmVals.length
            if (len == 1) {
              QParamSg(nmVals(0), None)
            } else if (len == 2) {
              QParamSg(nmVals(0), Some(nmVals(1)))
            } else {
              throw new RuntimeException("invalida query param part " + nmVal)
            }
          }
        }

      val fragment = Option(uri.getFragment)
      
      Path(base, PathSg(pathParts.toList), params, fragment)
    }
  }
}