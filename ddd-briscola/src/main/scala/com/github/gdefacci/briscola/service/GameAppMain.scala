package com.github.gdefacci.briscola.service

import com.github.gdefacci.briscola.service.impl.simple._

import com.github.gdefacci.di._

object GameAppMain extends scala.App {

  IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)
  println(IOC.getSource[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories))

}

