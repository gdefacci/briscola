package org.obl.brisola.webtest

//import org.obl.briscola.player.{Players, TeamPlayers, GamePlayers}
import org.obl.briscola.presentation._
import argonaut.DecodeJson
import org.obl.briscola.web.util.UrlParseUtil
import argonaut.DecodeResult
import argonaut.JsonParser
import argonaut.Json
import argonaut.CursorHistory
import scalaz.{-\/, \/, \/-}
import org.obl.raz.Path
import argonaut.JObject

trait TestDecoders {
  
  case class OutPlayer(self:Path, name:String)
  
  implicit val pathDecoder = DecodeJson[org.obl.raz.Path] { j =>
    j.as[String].flatMap { str =>
      UrlParseUtil.parseUrl(str) match {
        case None => DecodeResult.fail(s"invalid path ${str}",j.history)
        case Some(pth) => DecodeResult.ok(pth)
      }  
    }
    
  }
  
  implicit lazy val siteMapDecode = DecodeJson.derive[SiteMap]

  lazy val privatePlayerDecode = DecodeJson.derive[Player]
  
  lazy val outPlayerDecode = DecodeJson.derive[OutPlayer]

  lazy val playersDecode = DecodeJson[Collection[OutPlayer]] { j =>
    j.as[Map[String, Json]].flatMap { mp =>
      mp.get("members") match {
        
        case None => 
          DecodeResult.fail(s"missing 'mebers' property ${j}",j.history)
          
        case Some(j) =>
          j.as[Seq[Json]].flatMap { seq => 
            val z:(String, CursorHistory) \/ Seq[OutPlayer] = \/-(Nil)
            seq.foldLeft(z) { (acc, ji) =>
              acc.flatMap { players =>
                outPlayerDecode.decodeJson(ji).toDisjunction.map( players :+ _ )
              }
            } match {
              case -\/((str, curs)) => DecodeResult.fail(str, curs)
              case \/-(pls) => DecodeResult.ok(Collection(pls))
            } 
          }
          
      }
    }
  } 
  
//  implicit lazy val gamePlayersDecode = {
//    
//    val playersIdDecode = pathDecoder.map(PlayerId(_))
//    val playersDecode = DecodeJson.derive[Players]
//    
//  }
  
  implicit lazy val competitionStateDecode = {
    implicit val matchKindDecode = {
      val singleMatchDecode = DecodeJson[MatchKind] { j =>
        j.as[String].flatMap { 
          case "SingleMatch" => DecodeResult.ok(SingleMatch)  
          case x => DecodeResult.fail("no SingleMatch", j.history)
        }
      }
      
      singleMatchDecode ||| 
        DecodeJson.derive[NumberOfGamesMatchKind].map( d => d:MatchKind ) ||| 
        DecodeJson.derive[TargetPointsMatchKind].map( d => d:MatchKind )
    }
    
    implicit val competitionStartDeadline = {
      val allPlayersDecode = DecodeJson[CompetitionStartDeadline] { j =>
        j.as[String].flatMap { 
          case "AllPlayers" => DecodeResult.ok(AllPlayers)  
          case x => DecodeResult.fail("no AllPlayers", j.history)
        }
      }
      allPlayersDecode ||| DecodeJson.derive[OnPlayerCount].map(d => d:CompetitionStartDeadline)
    }
    
//    DecodeJson.derive[CompetitionState]
  }
  
  def decode[T](str:String)(implicit dj:DecodeJson[T]) = {
    JsonParser.parse(str).flatMap( dj.decodeJson(_).toDisjunction )
  }
}