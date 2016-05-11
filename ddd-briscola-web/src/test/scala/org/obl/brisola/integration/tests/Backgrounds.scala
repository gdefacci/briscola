package org.obl.brisola.integration
package tests

import org.obl.free._
import org.obl.briscola.presentation.{ CompetitionState, Player }
import org.obl.briscola.web.BriscolaWebApp

import org.obl.briscola.player.{ Player => DomainPlayer, Players => DomainPlayers }

import org.obl.briscola.competition.{ SingleMatch, CompetitionStartDeadline }
import scalaz.{ -\/, \/, \/- }
import org.obl.ddd.DomainError
import scalaz.WriterT

case class CompetionWith3Players(player1: Player, player2: Player, player3: Player, competition: CompetitionState)

case class GenericDomainError(error: Throwable) extends DomainError

object Backgrounds {

  import BriscolaIntegrationTest._

  import scalaz.std.list._

  private def fromDomainErrorDisj[T](v: DomainError \/ T): Throwable \/ T = {
    v match {
      case -\/(err) => -\/(new Exception(err.toString))
      case \/-(v) => \/-(v)
    }
  }

  def create[T](f: BriscolaWebApp => (DomainError \/ (BriscolaWebApp, T))) = TestState[T](v1 => fromDomainErrorDisj(f(v1)))

  lazy val void: Background[Unit] = WriterT.put(create[Unit] { webApp => \/-(webApp, ()) })("empty initial state" :: Nil)

  def createPlayer(name: String, pws: String): Background[DomainPlayer] = WriterT.put(create[DomainPlayer] { webApp =>
    for {
      pl1 <- webApp.app.playerService.createPlayer(name, pws)
    } yield (webApp -> pl1)
  })(s"$name is sucessfully logged" :: Nil)

  lazy val given3PlayersAndPlayer1CreateACompetion = for {
    pl1 <- createPlayer("player1", "psw1")
    pl2 <- createPlayer("player2", "psw2")
    pl3 <- createPlayer("player3", "psw3")
    res <- WriterT.put(create[CompetionWith3Players] { webApp =>
      for {
        genComp <- webApp.app.competitionService.createCompetition(pl1.id, DomainPlayers(Set(pl2.id, pl3.id)), SingleMatch, CompetitionStartDeadline.AllPlayers)
        comp <- genComp match {
          case c: org.obl.briscola.competition.ClientCompetitionState => \/-(c)
          case c => -\/(GenericDomainError(new Exception(s"expecting a ClientCompetitionState got $c")))
        }
        toPresentation = webApp.playerPresentationAdapter(_: DomainPlayer)
      } yield {
        webApp -> CompetionWith3Players(toPresentation(pl1), toPresentation(pl2), toPresentation(pl3), webApp.competitionPresentationAdapter(comp, None))
      }
    })("player1 create a competion" :: Nil)
  } yield res

}

