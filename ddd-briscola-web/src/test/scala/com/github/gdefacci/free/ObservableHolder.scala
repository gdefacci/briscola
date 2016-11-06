package com.github.gdefacci.free

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import rx.lang.scala.Observable

case class ObservableHolder[T](source:Observable[T]) {
  
  lazy val logger = Logger(LoggerFactory.getLogger(getClass))

  private val buffer = collection.mutable.Buffer.empty[T]
  
  source.foreach { msg =>
    logger.debug("received message from web socket")
    logger.debug(msg.toString)
    buffer += msg 
  }
  
  def messages:Seq[T] = buffer.toSeq
  
}