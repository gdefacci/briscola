package com.github.gdefacci.briscola.presentation.game

import com.github.gdefacci.briscola.{game => model}

import com.github.gdefacci.briscola.presentation
import com.github.gdefacci.briscola.web.util.ArgonautHelper.{enumDecoder, fromMap}

import argonaut.DecodeJson
import argonaut.DecodeResult

object GameJsonDecoders {

  implicit val pathDecoder = com.github.gdefacci.briscola.web.util.ArgonautHelper.pathDecoder
  
  implicit val seedDecoder = DecodeJson[model.Seed] { j =>
    import model.Seed._
    j.as[String].map {
      case "bastoni" => bastoni
      case "coppe" => coppe
      case "denari" => denari
      case "spade" => spade
    }
  }

  implicit val cardDecoder = DecodeJson[model.Card] { j =>
    for (
      number <- (j --\ "number").as[Int];
      seed <- (j --\ "seed").as[model.Seed]
    ) yield model.Card(number.toByte, seed)
  }

//  implicit lazy val matchKindDecoder: DecodeJson[MatchKind] = {
//
//    lazy val singleMatchDecoder = fromMap[String, MatchKind](Map("single-match" -> SingleMatch), s"invalid MatchKind") 
//
//    lazy val numberOfGamesMatchKindEncoder = DecodeJson.derive[NumberOfGamesMatchKind].map[MatchKind](p => p)
//    
//    lazy val targetPointsMatchKindEncoder = DecodeJson.derive[TargetPointsMatchKind].map[MatchKind](p => p) 
//
//    singleMatchDecoder ||| numberOfGamesMatchKindEncoder ||| targetPointsMatchKindEncoder
//  }
//
//  implicit lazy val competitionStartDeadlineDecoder: DecodeJson[CompetitionStartDeadline] = {
//
//    lazy val allPlayersDecoder = fromMap[String, CompetitionStartDeadline](Map("all-players" -> CompetitionStartDeadline.AllPlayers), s"invalid MatchKind") 
//
//    lazy val onPlayerCountEncoder = DecodeJson.derive[CompetitionStartDeadline.OnPlayerCount].map[CompetitionStartDeadline](p => p)
//
//    allPlayersDecoder ||| onPlayerCountEncoder
//  }
//
//  implicit lazy val teamPlayerDecoder:DecodeJson[Input.TeamPlayer] = DecodeJson.derive[Input.TeamPlayer]  
//  implicit lazy val teamInfoDecoder:DecodeJson[Input.TeamInfo] = DecodeJson.derive[Input.TeamInfo]  
//  
//  implicit lazy val teamDecoder:DecodeJson[Input.Team] = DecodeJson.derive[Input.Team]  
//  implicit lazy val teamsDecoder:DecodeJson[Input.Teams] = DecodeJson.derive[Input.Teams]  
//  
//  implicit lazy val gamePlayersDecoder = {
//    
//    lazy val playersDecoder:DecodeJson[Input.GamePlayers] = DecodeJson[Input.GamePlayers] { j =>
//      j.as[Set[org.obl.raz.Path]].map(Input.Players(_):Input.GamePlayers)
//    }
//    
//    lazy val teamPlayersDecoder:DecodeJson[Input.GamePlayers] = DecodeJson.derive[Input.TeamPlayers].map[Input.GamePlayers](p=>p)  
//        
//    playersDecoder ||| teamPlayersDecoder
//  }
//
//  implicit lazy val competitionDecoder:DecodeJson[Input.Competition] = DecodeJson.derive[Input.Competition]
//  
}