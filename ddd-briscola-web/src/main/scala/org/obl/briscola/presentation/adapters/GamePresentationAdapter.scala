package org.obl.briscola.presentation.adapters

import org.obl.briscola.presentation
import org.obl.briscola.web.GameRoutes
import org.obl.briscola.web.PlayerRoutes
import org.obl.briscola._
import org.obl.briscola.player.PlayerId
import org.obl.briscola.web.PresentationAdapter
import org.obl.briscola.service.player.PlayerGameEvent
import org.obl.briscola.service.player.PlayerActiveGameState

object GamePresentationAdapter {
  def apply(gr: => GameRoutes, pr: => PlayerRoutes) = {
    new GamePresentationAdapter {
      lazy val gameRoutes = gr
      lazy val playerRoutes = pr
    }
  }
}

trait GamePresentationAdapter {
  
  def gameRoutes: GameRoutes
  def playerRoutes: PlayerRoutes

  lazy val cardAdapter = PresentationAdapter((card:Card)  => 
    presentation.Card(card.number, card.seed, card.points) )
  
  implicit lazy val playerStateAdapter = PresentationAdapter((ps: PlayerState) =>
    presentation.PlayerState(playerRoutes.PlayerById.encode(ps.id), ps.cards.map(cardAdapter(_)), presentation.Score(ps.score.cards.map( c => cardAdapter(c))) ))

  implicit lazy val playerFinalStateAdapter = PresentationAdapter((ps: PlayerFinalState) =>
    presentation.PlayerFinalState(playerRoutes.PlayerById.encode(ps.id), ps.points, presentation.Score(ps.score.cards.map(cardAdapter(_))) ))

  lazy val playerLeftAdapter = PresentationAdapter((ps: PlayerLeft) =>
    presentation.PlayerLeft(playerRoutes.PlayerById.encode(ps.player), ps.reason))
  
  lazy val dropReasonAdapter = PresentationAdapter((ps: DropReason) => ps match {
    case pl:PlayerLeft => playerLeftAdapter(pl)
  })

  lazy val teamScoreAdapter = PresentationAdapter((ts:TeamScore) => {
    presentation.TeamScore(
        ts.team.name, 
        ts.team.players.map(pid => playerRoutes.PlayerById.encode(pid)), 
        ts.score.cards.map(cardAdapter(_)), 
        ts.score.points )
  })    
      
  lazy val droppedGameStateAdapter = PresentationAdapter((gm: DroppedGameState) =>
    presentation.DroppedGameState(
      gameRoutes.GameById.encode(gm.id),
      cardAdapter(gm.briscolaCard), 
      gm.teams.map(tms => tms.teams.map( t => gameRoutes.Team.encode(gm.id, t.name))),
      gm.moves.map(m => presentation.Move(playerRoutes.PlayerById.encode(m.player.id), cardAdapter(m.card))),
      gm.nextPlayers.map(p => playerRoutes.PlayerById.encode(p.id)),
      dropReasonAdapter(gm.dropReason)))
      
  implicit lazy val finalGameStateAdapter = PresentationAdapter((gm: FinalGameState) => {
    val teamResult = for (teamOrderByPoint <- gm.teamScoresOrderByPoints; winnerTeam <- gm.winnerTeam) yield(
        presentation.TeamsGameResult(teamOrderByPoint.map(teamScoreAdapter(_)), teamScoreAdapter(winnerTeam))
    )
    
    val gmr = teamResult.getOrElse(presentation.PlayersGameResult(gm.playersOrderByPoints.map(playerFinalStateAdapter(_)), playerFinalStateAdapter(gm.winner)))
    
    presentation.FinalGameState(
        gameRoutes.GameById.encode(gm.id),
        cardAdapter(gm.briscolaCard), gmr)
  })
  

  def toActiveGameState(gm: ActiveGameState, player: Option[PlayerId]): presentation.ActiveGameState =
    presentation.ActiveGameState(
      gameRoutes.GameById.encode(gm.id),
      cardAdapter(gm.briscolaCard), 
      gm.teams.map(tms => tms.teams.map( t => gameRoutes.Team.encode(gm.id, t.name))),
      gm.moves.map(m => presentation.Move(playerRoutes.PlayerById.encode(m.player.id), cardAdapter(m.card))),
      gm.nextPlayers.map(p => playerRoutes.PlayerById.encode(p.id)),
      playerRoutes.PlayerById.encode(gm.currentPlayer.id),
      gm.isLastHandTurn, gm.isLastGameTurn,
      gm.players.map(p => playerRoutes.PlayerById.encode(p.id)),
      player.map(pid => gameRoutes.Player.encode(gm.id, pid)),
      gm.deckCardsNumber)

  
  def toGameState(gm: GameState, player: Option[PlayerId]): presentation.GameState = gm match {
    case EmptyGameState => presentation.EmptyGameState
    case gm: ActiveGameState => toActiveGameState(gm, player)
    case gm: DroppedGameState => droppedGameStateAdapter(gm)
    case gm: FinalGameState => finalGameStateAdapter(gm)
  }
  

  implicit lazy val gameStateAdapter = PresentationAdapter((gm: GameState) => toGameState(gm, None))

  implicit lazy val playerGameEventAdater = PresentationAdapter[PlayerGameEvent, presentation.BriscolaEvent]((ev:PlayerGameEvent) => ev.event match {
    case GameStarted(gm) => presentation.GameStarted(toActiveGameState(gm, Some(ev.playerId)))
    case GameDropped(dropReason) => presentation.GameDropped(gameRoutes.GameById.encode(ev.gameId), dropReasonAdapter(dropReason) )
    case CardPlayed(pid, crd) => presentation.CardPlayed(
      gameRoutes.GameById.encode(ev.gameId),
      playerRoutes.PlayerById.encode(ev.playerId),
      cardAdapter(crd))
  })

  implicit lazy val playerActiveGameStateAdapter = PresentationAdapter( (pags:PlayerActiveGameState) => 
    toActiveGameState(pags.game, Some(pags.playerId)) 
  )

}

