package org.obl.brisola.free

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import rx.lang.scala.Observable
import argonaut.JsonParser
import argonaut.DecodeJson

trait HttpTestHelper {

  def isSuccess(code: Int) = code >= 200 && code < 300

}

trait RxTestHelper {

  private lazy val log = Logger(LoggerFactory.getLogger(getClass))
  
  def contains[T](obs:Observable[String], pred:T => Boolean)(implicit decoder:DecodeJson[T]):Boolean = {
    obs.toBlocking.toList.exists { text =>
      log.debug("°"*160)
      log.debug(s"receiving text $text\n")
      JsonParser.parse(text).flatMap(decoder.decodeJson(_).toDisjunction).map(pred).getOrElse(false)
    }
  }

}
