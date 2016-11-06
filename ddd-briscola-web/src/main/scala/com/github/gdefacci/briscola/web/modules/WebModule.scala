package com.github.gdefacci.briscola.web
package modules

import com.github.gdefacci.briscola.player.PlayerId

import com.github.gdefacci.briscola.web.util.{ WebSocketChannel, ServletPlan, ToPresentation }

import com.github.gdefacci.briscola.presentation.competition.CompetitionPresentationAdapter
import com.github.gdefacci.briscola.presentation.player.PlayerPresentationAdapter

import javax.inject.Singleton
import com.github.gdefacci.di.runtime.{ Bind, AllBindings }
import com.github.gdefacci.briscola.presentation.game.GamePresentationAdapter
import com.github.gdefacci.briscola.presentation.RoutesServletConfig
import com.github.gdefacci.briscola.presentation.BriscolaWebApp
import com.github.gdefacci.briscola.presentation.Resources

object WebModule {

  val routesServletConfig = RoutesServletConfig

  @Singleton
  def webApp(plans: AllBindings[ServletPlan], channels: AllBindings[WebSocketChannel[PlayerId]]) =
    new BriscolaWebApp(plans.values, WebSocketChannel.merge(channels.values))

  def errorToPresentation[T] = {
    import org.http4s.dsl._
    new ToPresentation[T](err => InternalServerError(err.toString))
  }

  @Singleton val bindResources = Bind.bind[Resources]
  @Singleton val bindGamePresentationAdapter = Bind.bind[GamePresentationAdapter]
  @Singleton val bindCompetitionPresentationAdapter = Bind.bind[CompetitionPresentationAdapter]
  @Singleton val bindPlayerPresentationAdapter = Bind.bind[PlayerPresentationAdapter]

}