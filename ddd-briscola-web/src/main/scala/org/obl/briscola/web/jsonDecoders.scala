package org.obl.briscola.web

import org.obl.briscola.Card
import org.obl.briscola.Seed
import org.obl.briscola.competition.CompetitionStartDeadline
import org.obl.briscola.competition.CompetitionStartDeadline.AllPlayers
import org.obl.briscola.competition.CompetitionStartDeadline.OnPlayerCount
import org.obl.briscola.competition.MatchKind
import org.obl.briscola.competition.NumberOfGamesMatchKind
import org.obl.briscola.competition.SingleMatch
import org.obl.briscola.competition.TargetPointsMatchKind
import org.obl.briscola.player.PlayerId
import org.obl.briscola.web.util.ArgonautHelper.enumDecoder
import org.obl.briscola.web.util.ArgonautHelper.pathDecoder
import org.obl.briscola.presentation

import argonaut.DecodeJson
import argonaut.DecodeResult

object jsonDecoders {
  
  implicit val playerDecoder = DecodeJson[presentation.Input.Player] { j =>
    for (
      name <- (j --\ "name").as[String];
      psw <- (j --\ "password").as[String]
    ) yield presentation.Input.Player(name, psw)
  }

  implicit val seedDecoder = enumDecoder(Seed) 

  implicit val cardDecoder = DecodeJson[Card] { j =>
    for (
      number <- (j --\ "number").as[Int];
      seed <- (j --\ "seed").as[Seed.Value]
    ) yield Card(number.toByte, seed)
  }

  trait PlayerRoutesJsonDecoders {
    val playerRoutes: PlayerRoutes
    
    private implicit lazy val playerIdDecoder:DecodeJson[PlayerId] = {
      import playerRoutes._      
      pathDecoder(PlayerById.decoderWrap)
    }

    private implicit lazy val matchKindDecoder:DecodeJson[MatchKind] = {

      lazy val singleMatchDecoder = DecodeJson[MatchKind] { j =>
        j.as[String].flatMap {
          case "single-match" => DecodeResult.ok(SingleMatch)
          case x => DecodeResult.fail(s"invalid MatchKind: $x", j.history)
        }
      }

      lazy val numberOfGamesMatchKindEncoder = DecodeJson[MatchKind] { j =>
        for (
          number <- (j --\ "numberOfMatches").as[Int]
        ) yield NumberOfGamesMatchKind(number)
      }

      lazy val targetPointsMatchKindEncoder = DecodeJson[MatchKind] { j =>
        for (
          number <- (j --\ "winnerPoints").as[Int]
        ) yield TargetPointsMatchKind(number)
      }

      singleMatchDecoder ||| numberOfGamesMatchKindEncoder ||| targetPointsMatchKindEncoder
    }

    private implicit lazy val competitionStartDeadlineDecoder:DecodeJson[CompetitionStartDeadline] = {

      lazy val allPlayersDecoder = DecodeJson[CompetitionStartDeadline] { j =>
        j.as[String].flatMap {
          case "all-players" => DecodeResult.ok(CompetitionStartDeadline.AllPlayers)
          case x => DecodeResult.fail(s"invalid MatchKind: $x", j.history)
        }
      }

      lazy val onPlayerCountEncoder = DecodeJson[CompetitionStartDeadline] { j =>
        for (
          number <- (j --\ "numberOfMatches").as[Int]
        ) yield OnPlayerCount(number)
      }

      allPlayersDecoder ||| onPlayerCountEncoder
    }

    implicit lazy val competitionEncoder = {
      DecodeJson[presentation.Input.Competition] { j =>
        for (
          players <- (j --\ "players").as[Seq[PlayerId]];
          kind <- (j --\ "kind").as[Option[MatchKind]];
          deadline <- (j --\ "deadline").as[Option[CompetitionStartDeadline]]
        ) yield {
          presentation.Input.Competition(players, kind.getOrElse(SingleMatch), deadline.getOrElse(AllPlayers))
        }
      }

    }

  }
}