package com.github.gdefacci.briscola.presentation.competition

import com.github.gdefacci.briscola.{ competition => model }

import com.github.gdefacci.briscola.web.util.ArgonautHelper.{ enumDecoder, fromMap }

import argonaut.DecodeJson
import argonaut.DecodeResult

object CompetitionJsonDecoders {

  import com.github.gdefacci.briscola.presentation.CommonJsonDecoders.SeqDecodeJson
  import com.github.gdefacci.briscola.presentation.game.GameJsonDecoders._

  implicit lazy val matchKindDecoder: DecodeJson[model.MatchKind] = {

    lazy val singleMatchDecoder = fromMap[String, model.MatchKind](Map("single-match" -> model.SingleMatch), s"invalid MatchKind")

    lazy val numberOfGamesMatchKindEncoder = DecodeJson.derive[model.NumberOfGamesMatchKind].map[model.MatchKind](p => p)

    lazy val targetPointsMatchKindEncoder = DecodeJson.derive[model.TargetPointsMatchKind].map[model.MatchKind](p => p)

    singleMatchDecoder ||| numberOfGamesMatchKindEncoder ||| targetPointsMatchKindEncoder
  }

  implicit lazy val competitionStartDeadlineDecoder: DecodeJson[model.CompetitionStartDeadline] = {

    lazy val allPlayersDecoder = fromMap[String, model.CompetitionStartDeadline](Map("all-players" -> model.CompetitionStartDeadline.AllPlayers), s"invalid MatchKind")

    lazy val onPlayerCountEncoder = DecodeJson.derive[model.CompetitionStartDeadline.OnPlayerCount].map[model.CompetitionStartDeadline](p => p)

    allPlayersDecoder ||| onPlayerCountEncoder
  }
  
//  implicit def seqDecodeJson[T]() = DecodeJson[Seq[T]]
//  implicit def SeqDecodeJson[A](implicit e: DecodeJson[A]): DecodeJson[Seq[A]] = DecodeJson.CanBuildFromDecodeJson[A, Seq]


  implicit lazy val teamPlayerDecoder: DecodeJson[Input.TeamPlayer] = DecodeJson.derive[Input.TeamPlayer]
  implicit lazy val teamInfoDecoder: DecodeJson[Input.TeamInfo] = DecodeJson.derive[Input.TeamInfo]

  implicit lazy val teamDecoder: DecodeJson[Input.Team] = DecodeJson.derive[Input.Team]
  implicit lazy val teamsDecoder: DecodeJson[Input.Teams] = DecodeJson.derive[Input.Teams]

  implicit lazy val gamePlayersDecoder = {

    lazy val playersDecoder: DecodeJson[Input.GamePlayers] = DecodeJson[Input.GamePlayers] { j =>
      j.as[Set[org.obl.raz.Path]].map(Input.Players(_): Input.GamePlayers)
    }

    lazy val teamPlayersDecoder: DecodeJson[Input.GamePlayers] = DecodeJson.derive[Input.TeamPlayers].map[Input.GamePlayers](p => p)

    playersDecoder ||| teamPlayersDecoder
  }

  implicit lazy val competitionDecoder: DecodeJson[Input.Competition] = DecodeJson.derive[Input.Competition]

}