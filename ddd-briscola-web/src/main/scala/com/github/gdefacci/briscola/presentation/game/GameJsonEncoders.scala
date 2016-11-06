package com.github.gdefacci.briscola.presentation.game

import argonaut._
import Argonaut._
import org.obl.raz.Path

import com.github.gdefacci.briscola.web.util.ArgonautHelper._
import com.github.gdefacci.briscola.game.Seed

import EncodeJson.derive

object GameJsonEncoders {

  import com.github.gdefacci.briscola.presentation.CommonJsonEncoders._
  
  implicit lazy val seedEncoder = EncodeJson[Seed] { v => Json.jString(v.toString) }
      
  implicit lazy val cardEncoder = {
    implicit val byteEnc = EncodeJson.IntEncodeJson.contramap((b:Byte) => b.toInt)
    EncodeJson.derive[Card]
  }
  
  implicit lazy val playerScore = EncodeJson.derive[Score]
  
  implicit lazy val playerStateEncoder = EncodeJson.derive[PlayerState]

  implicit lazy val playerFinalStateEncoder = EncodeJson.derive[PlayerFinalState]
  
  implicit lazy val moveEncoder = EncodeJson.derive[Move] 

  implicit lazy val gameStateKindEncoder = enumEncoder[GameStateKind.type]
  
  implicit lazy val dropReasonEncoder = {
    
    implicit lazy val dropReasonKindEncoder = enumEncoder[DropReasonKind.type]
    
    lazy val playerLeftDropReasonEncoder:EncodeJson[PlayerLeft] = 
      withKind[DropReasonKind.type]( EncodeJson.derive[PlayerLeft] )
    
    jencode1((p: DropReason) => p match {
      case gm:PlayerLeft => playerLeftDropReasonEncoder(gm)
    })
  }
  
  implicit lazy val activeGameEncoder = withKind[GameStateKind.type](derive[ActiveGameState]) 
  
  implicit lazy val teamScore = derive[TeamScore]
  
  implicit lazy val gameResultEncoder:EncodeJson[GameResult] = {
    jencode1((p: GameResult) => p match {
      case tgmr:TeamsGameResult => derive[TeamsGameResult](tgmr)
      case pgmr:PlayersGameResult => derive[PlayersGameResult](pgmr)
    })    
  }
  
  implicit lazy val gameStateEncoder = {
    
    lazy val emptyGameEncoder = singletonADTEncoder[GameStateKind.type, EmptyGameState.type]
  
    lazy val finalGameEncoder = withKind[GameStateKind.type](derive[FinalGameState]) 
    
    lazy val droppedGameEncoder = withKind[GameStateKind.type](derive[DroppedGameState]) 
        
    jencode1((p: GameState) => p match {
      case EmptyGameState => emptyGameEncoder(EmptyGameState)
      case gm:ActiveGameState => activeGameEncoder(gm)
      case gm:DroppedGameState => droppedGameEncoder(gm)
      case gm:FinalGameState => finalGameEncoder(gm)
    })    
      
  }   
  
  implicit lazy val briscolaEventEncoder = {
    implicit lazy val gameEventKindEncoder = enumEncoder[BriscolaEventKind.type]
    
    lazy val gameStartedEventEncoder = withKind[BriscolaEventKind.type](derive[GameStarted])
    lazy val cardPlayedEventEncoder = withKind[BriscolaEventKind.type](derive[CardPlayed]) 
    lazy val gameDroppedEventEncoder = withKind[BriscolaEventKind.type](derive[GameDropped]) 
      
    jencode1((p: BriscolaEvent) => p match {
      case c:GameStarted => gameStartedEventEncoder(c)
      case c:CardPlayed => cardPlayedEventEncoder(c)
      case c:GameDropped => gameDroppedEventEncoder(c)
    })  
  }
}