package com.github.gdefacci.briscola.presentation.game

import com.github.gdefacci.briscola.{game => model}
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.web.util.PresentationAdapter
import com.github.gdefacci.briscola.presentation.player.PlayerRoutes
import com.github.gdefacci.briscola.presentation.PlayerGameEvent
import com.github.gdefacci.briscola.presentation.PlayerActiveGameState

class GamePresentationAdapter(gameRoutes: GameRoutes, playerRoutes: PlayerRoutes) {
  
  lazy val cardAdapter = PresentationAdapter((card:model.Card)  => 
    Card(card.number, card.seed, card.points) )
  
  implicit lazy val playerStateAdapter = PresentationAdapter((ps: model.PlayerState) =>
    PlayerState(playerRoutes.PlayerById.encode(ps.id), ps.cards.map(cardAdapter(_)), Score(ps.score.cards.map( c => cardAdapter(c))) ))

  implicit lazy val playerFinalStateAdapter = PresentationAdapter((ps: model.PlayerFinalState) =>
    PlayerFinalState(playerRoutes.PlayerById.encode(ps.id), ps.points, Score(ps.score.cards.map(cardAdapter(_))) ))

  lazy val playerLeftAdapter = PresentationAdapter((ps: model.PlayerLeft) =>
    PlayerLeft(playerRoutes.PlayerById.encode(ps.player), ps.reason))
  
  lazy val dropReasonAdapter = PresentationAdapter((ps: model.DropReason) => ps match {
    case pl:model.PlayerLeft => playerLeftAdapter(pl)
  })

  lazy val teamScoreAdapter = PresentationAdapter((ts:model.TeamScore) => {
    TeamScore(
        ts.team.name, 
        ts.team.players.map(pid => playerRoutes.PlayerById.encode(pid)), 
        ts.score.cards.map(cardAdapter(_)), 
        ts.score.points )
  })    
      
  lazy val droppedGameStateAdapter = PresentationAdapter((gm: model.DroppedGameState) =>
    DroppedGameState(
      gameRoutes.GameById.encode(gm.id),
      cardAdapter(gm.briscolaCard), 
      gm.teams.map(tms => tms.teams.toSeq.map( t => gameRoutes.Team.encode(gm.id, t.name))),
      gm.moves.map(m => Move(playerRoutes.PlayerById.encode(m.player.id), cardAdapter(m.card))),
      gm.nextPlayers.map(p => playerRoutes.PlayerById.encode(p.id)),
      dropReasonAdapter(gm.dropReason)))
      
  implicit lazy val finalGameStateAdapter = PresentationAdapter((gm: model.FinalGameState) => {
    val teamResult = for (teamOrderByPoint <- gm.teamScoresOrderByPoints; winnerTeam <- gm.winnerTeam) yield(
        TeamsGameResult(teamOrderByPoint.map(teamScoreAdapter(_)), teamScoreAdapter(winnerTeam))
    )
    
    val gmr = teamResult.getOrElse(PlayersGameResult(gm.playersOrderByPoints.map(playerFinalStateAdapter(_)), playerFinalStateAdapter(gm.winner)))
    
    FinalGameState(
        gameRoutes.GameById.encode(gm.id),
        cardAdapter(gm.briscolaCard), gmr)
  })
  

  def toActiveGameState(gm: model.ActiveGameState, player: Option[PlayerId]): ActiveGameState =
    ActiveGameState(
      gameRoutes.GameById.encode(gm.id),
      cardAdapter(gm.briscolaCard), 
      gm.teams.map(tms => tms.teams.toSeq.map( t => gameRoutes.Team.encode(gm.id, t.name))),
      gm.moves.map(m => Move(playerRoutes.PlayerById.encode(m.player.id), cardAdapter(m.card))),
      gm.nextPlayers.map(p => playerRoutes.PlayerById.encode(p.id)),
      playerRoutes.PlayerById.encode(gm.currentPlayer.id),
      gm.isLastHandTurn, gm.isLastGameTurn,
      gm.players.map(p => playerRoutes.PlayerById.encode(p.id)),
      player.map(pid => gameRoutes.Player.encode(gm.id, pid)),
      gm.deckCardsNumber)

  
  def toGameState(gm: model.GameState, player: Option[PlayerId]): GameState = gm match {
    case model.EmptyGameState => EmptyGameState
    case gm: model.ActiveGameState => toActiveGameState(gm, player)
    case gm: model.DroppedGameState => droppedGameStateAdapter(gm)
    case gm: model.FinalGameState => finalGameStateAdapter(gm)
  }
  

  implicit lazy val gameStateAdapter = PresentationAdapter((gm: model.GameState) => toGameState(gm, None))

  implicit lazy val playerGameEventAdater = PresentationAdapter[PlayerGameEvent, BriscolaEvent]((ev:PlayerGameEvent) => ev.event match {
    case model.GameStarted(gm) => GameStarted(toActiveGameState(gm, Some(ev.playerId)))
    case model.GameDropped(dropReason) => GameDropped(gameRoutes.GameById.encode(ev.gameId), dropReasonAdapter(dropReason) )
    case model.CardPlayed(pid, crd) => CardPlayed(
      gameRoutes.GameById.encode(ev.gameId),
      playerRoutes.PlayerById.encode(ev.playerId),
      cardAdapter(crd))
  })

  implicit lazy val playerActiveGameStateAdapter = PresentationAdapter( (pags:PlayerActiveGameState) => 
    toActiveGameState(pags.game, Some(pags.playerId)) 
  )

}

