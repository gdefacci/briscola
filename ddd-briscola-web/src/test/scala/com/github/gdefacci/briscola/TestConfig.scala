package com.github.gdefacci.briscola

import org.obl.raz.Authority
import org.obl.raz.Path
import com.github.gdefacci.di.runtime.ModulesContainer
import com.github.gdefacci.di.IOC
import com.github.gdefacci.briscola.service.GameAppModule
import com.github.gdefacci.briscola.service.GameApp
import com.github.gdefacci.briscola.web.modules.WebModules
import com.github.gdefacci.briscola.web.modules.GameLayerModule

object TestConfModule {
  
  val authority = Authority("localhost", 8080)
  val contextPath = Path / "test"
  
}

class IntegrationTestModule extends ModulesContainer {
  
  import com.github.gdefacci.briscola.service.impl
  
  val conf = TestConfModule
  val gameLayerModule = new GameLayerModule(IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories))
  val webModules = WebModules
  
}