package org.obl.briscola.web.util

import scalaz._
import scalaz.std.option._
import scalaz.std.option.optionSyntax._

object TraverseUtils {
  
  def seqOption[L,R](o:Option[L \/ R]):L \/ Option[R] = o match {
    case None => \/-(None)
    case Some(-\/(err)) => -\/(err)
    case Some(\/-(v)) => \/-(Some(v))
  }
  
  def seqList[L,R](o:List[L \/ R]):L \/ List[R] = o.foldLeft(\/-(Nil):L \/ List[R] ) { (acc,i) =>
    for {
      lst <- acc
      item <- i 
    } yield (lst :+ item)
  }
  
  
}