package org.obl.briscola.integration.clients

import org.obl.free.StepFactory

trait StepFactoryHolder[S] {

  val stepFactory = new StepFactory[S]
}

