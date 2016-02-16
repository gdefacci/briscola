package org.obl.briscola.web

import org.obl.briscola.Card
import org.obl.briscola.Seed
import org.obl.briscola.competition.CompetitionStartDeadline
import org.obl.briscola.competition.MatchKind
import org.obl.briscola.competition.NumberOfGamesMatchKind
import org.obl.briscola.competition.SingleMatch
import org.obl.briscola.competition.TargetPointsMatchKind
import org.obl.briscola.presentation
import org.obl.briscola.presentation.Input
import org.obl.briscola.web.util.ArgonautHelper.{enumDecoder, fromMap}

import argonaut.DecodeJson
import argonaut.DecodeResult

object jsonDecoders {

  implicit val pathDecoder = org.obl.briscola.web.util.ArgonautHelper.pathDecoder
  
  implicit val playerDecoder = DecodeJson.derive[presentation.Input.Player]

  implicit val seedDecoder = enumDecoder(Seed)

  implicit val cardDecoder = DecodeJson[Card] { j =>
    for (
      number <- (j --\ "number").as[Int];
      seed <- (j --\ "seed").as[Seed.Value]
    ) yield Card(number.toByte, seed)
  }

  implicit lazy val matchKindDecoder: DecodeJson[MatchKind] = {

    lazy val singleMatchDecoder = fromMap[String, MatchKind](Map("single-match" -> SingleMatch), s"invalid MatchKind") 

    lazy val numberOfGamesMatchKindEncoder = DecodeJson.derive[NumberOfGamesMatchKind].map[MatchKind](p => p)
    
    lazy val targetPointsMatchKindEncoder = DecodeJson.derive[TargetPointsMatchKind].map[MatchKind](p => p) 

    singleMatchDecoder ||| numberOfGamesMatchKindEncoder ||| targetPointsMatchKindEncoder
  }

  implicit lazy val competitionStartDeadlineDecoder: DecodeJson[CompetitionStartDeadline] = {

    lazy val allPlayersDecoder = fromMap[String, CompetitionStartDeadline](Map("all-players" -> CompetitionStartDeadline.AllPlayers), s"invalid MatchKind") 

    lazy val onPlayerCountEncoder = DecodeJson.derive[CompetitionStartDeadline.OnPlayerCount].map[CompetitionStartDeadline](p => p)

    allPlayersDecoder ||| onPlayerCountEncoder
  }

  implicit lazy val teamPlayerDecoder:DecodeJson[Input.TeamPlayer] = DecodeJson.derive[Input.TeamPlayer]  
  implicit lazy val teamInfoDecoder:DecodeJson[Input.TeamInfo] = DecodeJson.derive[Input.TeamInfo]  
  
  implicit lazy val teamDecoder:DecodeJson[Input.Team] = DecodeJson.derive[Input.Team]  
  implicit lazy val teamsDecoder:DecodeJson[Input.Teams] = DecodeJson.derive[Input.Teams]  
  
  implicit lazy val gamePlayersDecoder = {
    
    lazy val playersDecoder:DecodeJson[Input.GamePlayers] = DecodeJson[Input.GamePlayers] { j =>
      j.as[Set[org.obl.raz.Path]].map(Input.Players(_):Input.GamePlayers)
    }
    
    lazy val teamPlayersDecoder:DecodeJson[Input.GamePlayers] = DecodeJson.derive[Input.TeamPlayers].map[Input.GamePlayers](p=>p)  
        
    playersDecoder ||| teamPlayersDecoder
  }

  implicit lazy val competitionDecoder:DecodeJson[Input.Competition] = DecodeJson.derive[Input.Competition]
  
}