package org.obl.briscola.integration
package tests

import org.obl.free._
import org.obl.briscola.presentation.{ CompetitionState, Player }
import org.obl.briscola.web.BriscolaWebApp

import org.obl.briscola.competition.{ SingleMatch, CompetitionStartDeadline }
import scalaz.{ -\/, \/, \/- }
import org.obl.ddd.DomainError
import scalaz.WriterT
import org.obl.briscola.web.AppRoutes

object Domain {
  type Player = org.obl.briscola.player.Player 
  type GamePlayers = org.obl.briscola.player.GamePlayers 
  val Players = org.obl.briscola.player.Players 
  type CompetitionState  = org.obl.briscola.competition.CompetitionState  
}



case class PlayerInfo(domain: Domain.Player, presentation: Player)
case class CompetitionStateInfo(domain: Domain.CompetitionState, presentation: CompetitionState)

case class CompetionWith3Players(player1: Player, player2: Player, player3: Player, competition: CompetitionState)

case class ApplicationRoutes(appRoutes:AppRoutes)

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

  lazy val void: Background[Unit] = Background("empty initial state", create[Unit] { webApp => \/-(webApp, ()) })

  def createPlayer(name: String, pws: String) = Background[PlayerInfo](s"$name is sucessfully logged", create[PlayerInfo] { webApp =>
    for {
      pl1 <- webApp.app.playerService.createPlayer(name, pws)
    } yield (webApp -> PlayerInfo(pl1, webApp.Players.presentationAdapter.playerAdapter(pl1)))
  })

  def createCompetition(issuer: Domain.Player, players: Domain.GamePlayers) = Background( s"${issuer.name} create a competion with ${players}" ,
    create[CompetitionStateInfo] { webApp =>
      for {
        genComp <- webApp.app.competitionService.createCompetition(issuer.id, players, SingleMatch, CompetitionStartDeadline.AllPlayers)
        comp <- genComp match {
          case c: org.obl.briscola.competition.ClientCompetitionState => \/-(c)
          case c => -\/(GenericDomainError(new Exception(s"expecting a ClientCompetitionState got $c")))
        }
      } yield {
        webApp -> CompetitionStateInfo(comp, webApp.Competitions.presentationAdapter.clientCompetitionStateAdapter(comp))
      }
    })

  lazy val given3PlayersAndPlayer1CreateACompetion = for {
    pl1 <- createPlayer("player1", "psw1")
    pl2 <- createPlayer("player2", "psw2")
    pl3 <- createPlayer("player3", "psw3")
    comp <- createCompetition(pl1.domain, Domain.Players(Set(pl2.domain.id, pl3.domain.id)))
  } yield CompetionWith3Players(pl1.presentation, pl2.presentation, pl3.presentation, comp.presentation)

  lazy val givenApplicationRoutes = Background[AppRoutes]("application routes", create( webApp => \/-(webApp -> webApp.routes) ))
  
}

