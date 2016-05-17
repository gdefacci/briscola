package org.obl.briscola.webtest

//import org.obl.briscola.player.{Players, TeamPlayers, GamePlayers}
import org.obl.briscola.presentation._
import argonaut.DecodeJson
import argonaut.DecodeResult
import argonaut.JsonParser
import argonaut.Json
import argonaut.CursorHistory
import scalaz.{ -\/, \/, \/- }
import org.obl.raz.Path
import argonaut.JObject
import org.obl.briscola.web.util.ArgonautHelper
import org.obl.briscola.web.jsonDecoders

trait TestDecoders {

  import org.obl.briscola.web.jsonDecoders.pathDecoder
  
  case class OutPlayer(self: Path, name: String)

  //implicit lazy val pathDecoder = ArgonautHelper.pathDecoder

  implicit lazy val siteMapDecode = DecodeJson.derive[SiteMap]

  lazy val privatePlayerDecode = DecodeJson.derive[Player]

  implicit lazy val outPlayerDecode = DecodeJson.derive[OutPlayer]

  lazy val playersDecode = DecodeJson[Collection[OutPlayer]] { j =>
    j.as[Map[String, Json]].flatMap { mp =>
      mp.get("members") match {

        case None =>
          DecodeResult.fail(s"missing 'mebers' property ${j}", j.history)

        case Some(j) =>
          j.as[Seq[Json]].flatMap { seq =>
            val z: (String, CursorHistory) \/ Seq[OutPlayer] = \/-(Nil)
            seq.foldLeft(z) { (acc, ji) =>
              acc.flatMap { players =>
                outPlayerDecode.decodeJson(ji).toDisjunction.map(players :+ _)
              }
            } match {
              case -\/((str, curs)) => DecodeResult.fail(str, curs)
              case \/-(pls) => DecodeResult.ok(Collection(pls))
            }
          }

      }
    }
  }

  implicit lazy val competitionStateDecode = {
    implicit val matchKindDecode = {
      val singleMatchDecode = DecodeJson[MatchKind] { j =>
        j.as[String].flatMap {
          case "SingleMatch" => DecodeResult.ok(SingleMatch)
          case x => DecodeResult.fail("no SingleMatch", j.history)
        }
      }

      singleMatchDecode |||
        DecodeJson.derive[NumberOfGamesMatchKind].map(d => d: MatchKind) |||
        DecodeJson.derive[TargetPointsMatchKind].map(d => d: MatchKind)
    }

    implicit val competitionStartDeadline = {
      val allPlayersDecode = DecodeJson[CompetitionStartDeadline] { j =>
        j.as[String].flatMap {
          case "AllPlayers" => DecodeResult.ok(AllPlayers)
          case x => DecodeResult.fail("no AllPlayers", j.history)
        }
      }
      allPlayersDecode ||| DecodeJson.derive[OnPlayerCount].map(d => d: CompetitionStartDeadline)
    }

    implicit lazy val competitionDecode = DecodeJson.derive[Competition]

    implicit lazy val competitionStateKindDecoder = ArgonautHelper.enumDecoder(CompetitionStateKind)

    DecodeJson.derive[CompetitionState]
  }
  
  implicit lazy val byteDecode = DecodeJson[Byte] (js => js.as[Int].map(_.toByte))
  
  implicit lazy val dropReason:DecodeJson[DropReason] = {
    
    lazy val playerLeftDropReason = ofKind(DropReasonKind.playerLeft, DecodeJson.derive[PlayerLeft])
    
    playerLeftDropReason.map( pl => pl:DropReason )
  }
  
  import jsonDecoders.seedDecoder
    
  implicit val cardDecode = DecodeJson.derive[Card] 
  implicit val moveDecode = DecodeJson.derive[Move] 
  
  implicit val gameResultDecode:DecodeJson[GameResult] = {
    
    implicit val scoreDecode = DecodeJson.derive[Score]
    implicit val teamScoreDecode = DecodeJson.derive[TeamScore]
	  implicit val teamsGameResultDecode = DecodeJson.derive[TeamsGameResult].map(p => p:GameResult)
	  implicit val playerFinalStateDecode = DecodeJson.derive[PlayerFinalState]
		
	  implicit val playersGameResultDecode = DecodeJson.derive[PlayersGameResult].map(p => p:GameResult)
	  
	  teamsGameResultDecode ||| playersGameResultDecode 
  }
  
  implicit val finishedGameStateDecode = ofKind(GameStateKind.finished, DecodeJson.derive[FinalGameState]) 
  implicit val activeGameStateDecode = ofKind(GameStateKind.active, DecodeJson.derive[ActiveGameState]) 
  implicit val droppedGameStateDecode = ofKind(GameStateKind.dropped, DecodeJson.derive[DroppedGameState])
  
  implicit lazy val gameStateDecode:DecodeJson[GameState] = finishedGameStateDecode.map(p => p:GameState) |||
    activeGameStateDecode.map(p => p:GameState) |||
    droppedGameStateDecode.map(p => p:GameState) 

  def decode[T](str: String)(implicit dj: DecodeJson[T]) = {
    JsonParser.parse(str).flatMap(dj.decodeJson(_).toDisjunction)
  }

  def decodePF[T](implicit dj: DecodeJson[T]): PartialFunction[String, T] = {
    case text if JsonParser.parse(text).flatMap( dj.decodeJson(_).toDisjunction ).isRight =>
      JsonParser.parse(text).flatMap( dj.decodeJson(_).toDisjunction ).toOption.get
  }
  
  def ofKind[E <: Enumeration, T <: ADT[E]](e:E#Value, dj:DecodeJson[T]):DecodeJson[T] = DecodeJson[T] { js =>
    val kstr = js.get[String]("kind")
    val toDecode = kstr.map(_ == e.toString).getOr(false)
    if (toDecode) dj.decode(js)
    else DecodeResult.fail(s"not a $e, got $js", js.history)
    
  }

  object CompetionEventDecoders {

    implicit lazy val CreatedCompetitionDecode = ofKind(CompetitionEventKind.createdCompetition, DecodeJson.derive[CreatedCompetition])
    implicit lazy val CompetitionAcceptedDecode = ofKind(CompetitionEventKind.playerAccepted, DecodeJson.derive[CompetitionAccepted])
    implicit lazy val CompetitionDeclinedDecode = ofKind(CompetitionEventKind.playerDeclined, DecodeJson.derive[CompetitionDeclined])

  }
  
  object GameEventDecoders {

    implicit lazy val GameStartedDecode = ofKind(BriscolaEventKind.gameStarted, DecodeJson.derive[GameStarted])
    implicit lazy val GameDroppedDecode = ofKind(BriscolaEventKind.gameDropped, DecodeJson.derive[GameDropped])
    implicit lazy val CardPlayedDecode = ofKind(BriscolaEventKind.cardPlayed, DecodeJson.derive[CardPlayed])

  }
  
  object PlayerEventDecoders {
    implicit lazy val PlayerLogOnDecode = ofKind(PlayerEventKind.playerLogOn, DecodeJson.derive[PlayerLogOn])
    implicit lazy val PlayerLogOffDecode = ofKind(PlayerEventKind.playerLogOff, DecodeJson.derive[PlayerLogOff])
  }


  implicit def stateAndEventDecoder[E, S](implicit decodeEvent: DecodeJson[E], decodeState: DecodeJson[S]): DecodeJson[EventAndState[E, S]] = {
    DecodeJson[EventAndState[E, S]] { j =>
      j.as[Map[String, Json]].flatMap { mp =>
        for {
          ev <- decodeEvent.decodeJson(mp("event"))
          state <- decodeState.decodeJson(mp("state"))
        } yield EventAndState[E, S](ev, state)
      }
    }
  }

}