package com.github.gdefacci.briscola.web
package modules

import com.github.gdefacci.briscola.presentation._
import com.github.gdefacci.briscola.presentation.competition._
import com.github.gdefacci.briscola.presentation.player._
import com.github.gdefacci.briscola.presentation.game._
import argonaut.EncodeJson
import com.github.gdefacci.briscola.presentation.CommonJsonEncoders.iterableEncodeJson

object JsonEncodeModule {
  
	def gameChannelJsonEncoder:EncodeJson[EventAndState[BriscolaEvent,GameState]] = {
	  import GameJsonEncoders._
    
    CommonJsonEncoders.eventAndState[BriscolaEvent,GameState]
	}

  def playerChannelJsonEncoder:EncodeJson[EventAndState[PlayerEvent, Iterable[Player]]] = {
    import PlayerJsonEncoders._
    
    CommonJsonEncoders.eventAndState[PlayerEvent, Iterable[Player]]
  }
  
  def competitionChannelJsonEncoder:EncodeJson[EventAndState[CompetitionEvent, CompetitionState]] = {
    import CompetitionJsonEncoders._
    
    CommonJsonEncoders.eventAndState
  }  
  
}