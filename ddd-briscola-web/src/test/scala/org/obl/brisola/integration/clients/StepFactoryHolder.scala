package org.obl.brisola.integration.clients

import org.obl.free.StepFactory

trait StepFactoryHolder[S] {

  val stepFactory = new StepFactory[S]
}

