package org.obl.briscola.web

import argonaut.DecodeJson
import scalaz.{ -\/, \/, \/- }
import org.obl.briscola.Seed
import argonaut.DecodeResult
import org.obl.briscola.Card
import org.obl.briscola.player.PlayerId
import org.obl.briscola.web.util.UriParseUtil
import org.obl.briscola.competition.SingleMatch
import org.obl.briscola.competition.Tournament
import org.obl.briscola.competition.TargetTournament
import org.obl.briscola.competition.MatchKind
import org.obl.briscola.competition.CompetitionStartDeadline
import org.obl.briscola.competition.CompetitionStartDeadline.OnPlayerCount
import org.obl.briscola.competition.CompetitionStartDeadline.AllPlayers

object jsonDecoders {

  implicit val playerDecoder = DecodeJson[Presentation.Input.Player] { j =>
    for (
      name <- (j --\ "name").as[String]
    ) yield Presentation.Input.Player(name)
  }

  implicit val seedDecoder = DecodeJson[Seed.Value] { j =>
    j.as[String].map(_.toLowerCase).flatMap {
      case "bastoni" => DecodeResult.ok(Seed.bastoni)
      case "coppe" => DecodeResult.ok(Seed.coppe)
      case "denari" => DecodeResult.ok(Seed.denari)
      case "spade" => DecodeResult.ok(Seed.spade)
      case x => DecodeResult.fail(s"invalid seeed $x", j.history)
    }
  }

  implicit val cardDecoder = DecodeJson[Card] { j =>
    for (
      number <- (j --\ "number").as[Int];
      seed <- (j --\ "seed").as[Seed.Value]
    ) yield Card(number.toByte, seed)
  }

  trait PlayerRoutesJsonDecoders {
    def playerRoutes: PlayerRoutes

    private implicit lazy val playerIdDecoder = DecodeJson[PlayerId] { j =>
      val errOrUri = j.as[String].toDisjunction.leftMap(_._1).flatMap { s =>
        UriParseUtil.parseUrl(s) match {
          case None => -\/(s"$s is not a valid url")
          case Some(v) => \/-(v)
        }
      }
      errOrUri match {
        case -\/(err) => DecodeResult.fail(err, j.history)
        case \/-(v) => v match {
          case playerRoutes.PlayerById(id) => DecodeResult.ok(id)
          case c => DecodeResult.fail(s"$c is not a valid player uri", j.history)
        }
      }
    }

    private implicit lazy val matchKindDecoder = {

      lazy val singleMatchDecoder = DecodeJson[MatchKind] { j =>
        j.as[String].flatMap {
          case "single-match" => DecodeResult.ok(SingleMatch)
          case x => DecodeResult.fail(s"invalid MatchKind: $x", j.history)
        }
      }

      lazy val tournamentEncoder = DecodeJson[MatchKind] { j =>
        for (
          number <- (j --\ "numberOfMatches").as[Int]
        ) yield Tournament(number)
      }

      lazy val targetTournamentEncoder = DecodeJson[MatchKind] { j =>
        for (
          number <- (j --\ "winnerPoints").as[Int]
        ) yield TargetTournament(number)
      }

      singleMatchDecoder ||| tournamentEncoder ||| targetTournamentEncoder
    }

    private implicit lazy val competitionStartDeadlineDecoder = {

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
      DecodeJson[Presentation.Input.Competition] { j =>
        for (
          players <- (j --\ "players").as[Set[PlayerId]];
          kind <- (j --\ "kind").as[Option[MatchKind]];
          deadlineKind <- (j --\ "deadlineKind").as[Option[CompetitionStartDeadline]]
        ) yield {
          Presentation.Input.Competition(players, kind.getOrElse(SingleMatch), deadlineKind.getOrElse(AllPlayers))
        }
      }

    }

  }
}