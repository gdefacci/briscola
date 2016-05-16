package org.obl.briscola.web

import org.obl.briscola.web.util.Plan
import org.obl.briscola.web.util.ServletPlan

trait PlansModule {
  def plans:Seq[ServletPlan]
}

trait ChannelModule[T] {
  def channel:WebSocketChannel[T]
}