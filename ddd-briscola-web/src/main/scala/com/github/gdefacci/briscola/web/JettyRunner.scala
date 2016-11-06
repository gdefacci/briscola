package com.github.gdefacci.briscola.web

import com.github.gdefacci.briscola.web.util.JettyServerFactory
import com.github.gdefacci.briscola.web.util.JettyWebAppConfig
import org.obl.raz.Authority
import org.obl.raz.Path
import com.github.gdefacci.briscola.web.util.ContainerConfiguratorImpl
import com.github.gdefacci.di.IOC
import com.github.gdefacci.briscola.service.GameApp
import com.github.gdefacci.briscola.service.GameAppModule
import com.github.gdefacci.briscola.web.modules._
import com.github.gdefacci.briscola.presentation.BriscolaWebAppConfig

object AppConfigFactory {

  import com.github.gdefacci.briscola.service.impl    
  
  val webAppConfig =    
    IOC.get[BriscolaWebAppConfig](
      new GameLayerModule(IOC.get[GameApp](new GameAppModule, impl.simple.idFactories, new impl.simple.repositories)),
      ConfModule,
      WebModules)

  class ConcretePlayerWebSocketEndPoint extends webAppConfig.ConfiguredPlayerWebSocketEndPoint

  lazy val configurator = new ContainerConfiguratorImpl[ConcretePlayerWebSocketEndPoint](
      webAppConfig.playerWebSocketRoutes.playerByIdUriTemplate, webAppConfig.webApp.plans)

}

object JettyRunner extends App {

  val appcfg = AppConfigFactory
  
  lazy val createJettyWebAppConfig: JettyWebAppConfig = {
    val context = JettyWebAppConfig.defaultWebAppContext()
    val appConfig = appcfg.webAppConfig
    context.setContextPath(appConfig.contextPath.render);
    JettyWebAppConfig(appConfig.host.port, context, appcfg.configurator)
  }
  
  val (server,_) = JettyServerFactory.createServers(createJettyWebAppConfig)

  server.start();
  server.dump(System.err);
  server.join();

}