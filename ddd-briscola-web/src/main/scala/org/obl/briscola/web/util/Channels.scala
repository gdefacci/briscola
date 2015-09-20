package org.obl.briscola.web.util

import scalaz.stream.async.mutable.Queue
import scalaz.stream.Process
import scalaz.concurrent.Task

class Channels[Id,T] {
  private val channelsMap = collection.concurrent.TrieMap.empty[Id, Queue[T]]
  
  def send(pl:Id, v:T):Unit = {
    val queue = channelsMap.get(pl).getOrElse {
      val nq = scalaz.stream.async.unboundedQueue[T]
      channelsMap += (pl -> nq)
      nq
    }
    queue.enqueueOne(v).run
  }
  
  def channel(pid:Id):Option[Process[Task, T]] = channelsMap.get(pid).map(_.dequeue)
}
