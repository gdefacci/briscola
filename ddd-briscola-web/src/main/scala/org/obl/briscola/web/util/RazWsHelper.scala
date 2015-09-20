package org.obl.briscola.web.util

import org.obl.raz.Path
import org.obl.raz.RelativePath
import org.obl.raz.AbsolutePath
import org.obl.raz.WS

object RazWsHelper extends App {

  import scala.language.existentials 
  
  def asWebSocket(p:Path) = {
    p match {
      case p:RelativePath[_,_] => p
      case p:AbsolutePath[_] => p.fragment match {
        case Some(frg) => AbsolutePath(WS(p.base.host, p.base.port), p.path, p.params, frg)
        case None => AbsolutePath(WS(p.base.host, p.base.port), p.path, p.params)
      }
    }
  }
  
}