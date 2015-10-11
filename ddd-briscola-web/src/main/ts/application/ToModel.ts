namespace Application.ToModel {

  import Path = Model.Ws.Path
  import Retrieve = Util.Retrieve
  import Fetch = Util.Fetch
  
  import CompetitionEventKind = Model.CompetitionEventKind
  import CompetitionStateKind = Model.CompetitionStateKind
  import GameStateKind = Model.GameStateKind
  import BriscolaEventKind = Model.BriscolaEventKind
  import PlayerEventKind = Model.PlayerEventKind
  import MatchKindKind = Model.MatchKindKind
  import CompetitionStartDeadlineKind = Model.CompetitionStartDeadlineKind
  import DropReasonKind = Model.DropReasonKind
  import Seed = Model.Seed
  
  export const siteMapFetch = (url:Path) => Fetch.GET<Model.Ws.SiteMap>(url)
  
  const playerFetch = (url:Path) => Fetch.GET<Model.Player>(url)

  const gameFetch: Fetch<Model.GameState> = (url:Path) =>
    Fetch.GET<Model.Ws.GameState>(url).then(ws => gameState(ws))

  const activeGameFetch: Fetch<Model.ActiveGameState> = (url:Path) =>
    Fetch.GET<Model.Ws.ActiveGameState>(url).then(ws => activeGameState(ws))
  
  const competitionFetch: Fetch<Model.CompetitionState> = (url:Path) =>
    Fetch.GET<Model.Ws.CompetitionState>(url).then(ws => competitionState(ws))

  const playerStateFetch: Fetch<Model.PlayerState> = (url:Path) =>
    Fetch.GET<Model.Ws.PlayerState>(url).then(ws => playerState(url)(ws))
  
  const seed: Retrieve<string, Seed> = ws =>
    Std.option(Seed[ws]).fold(
      () => Promise.reject(`invalid seed ${ws}`),
      (seed) => Promise.resolve(seed)
    )

  function logResult<T>(t:T):T {
    console.log("ToModel")
    console.log(t)
    return t;
  }
  
  const card: Retrieve<Model.Ws.Card, Model.Card> = ws => {
    if (ws === undefined) {
      console.log("")  
    }
    return seed(ws.seed).then( sd => { 
      return { 
        seed: sd, "number": 
        ws.number 
      } 
    })
  }

  const move: Retrieve<Model.Ws.Move, Model.Move> = ws =>
    playerFetch(ws.player).then(player =>
      card(ws.card).then(card => {
        return { player, card };
      })
    )

  export function playerState(self:Model.Ws.Path): Retrieve<Model.Ws.PlayerState, Model.PlayerState>  {
    return  ws => { 
      return Util.TPromise.all3(
        playerFetch(ws.player),
        Promise.all(ws.cards.map(c => card(c))),
        Promise.all(ws.score.map(c => card(c)))
      ).then(t => {
        return {
          self: self,
          player: t[0],
          cards: t[1],
          score: t[2]
        }
      })
    }
  }

  export const playerFinalState: Retrieve<Model.Ws.PlayerFinalState, Model.PlayerFinalState> = ws =>
    Util.TPromise.all2(
      playerFetch(ws.player),
      Promise.all(ws.score.map(c => card(c)))
    ).then(t => {
      return {
        player: t[0],
        points: ws.points,
        score: t[1]
      }
    })
  
  const playerLeft: Retrieve<Model.Ws.PlayerLeft, Model.PlayerLeft> = ws => playerFetch(ws.player).then(pl => 
    new Model.PlayerLeft(pl, Std.option(ws.reason))
  );

  export const dropReason: Retrieve<Model.Ws.DropReason, Model.DropReason> = ws => {
    switch (DropReasonKind[ws.kind]) {
      case DropReasonKind.playerLeft: return playerLeft(<any>ws)  
      default: return Promise.reject(`unexpected drop reason kind ${ws.kind}`)
    }
  }
  
  export const gameState: Retrieve<Model.Ws.GameState, Model.GameState> = ws => {
    switch (GameStateKind[ws.kind]) {
      case GameStateKind.active: return activeGameState(<any>ws)
      case GameStateKind.finished: return finalGameState(<any>ws)
      case GameStateKind.dropped: return droppedGameState(<any>ws)
      default: return Promise.reject(`unexpected game state kind ${ws.kind}`)
    }
  }

  export const activeGameState: Retrieve<Model.Ws.ActiveGameState, Model.ActiveGameState> = ws => 
    Util.TPromise.all6(
      card(ws.briscolaCard),
      Promise.all(ws.moves.map(m => move(m))),
      Promise.all(ws.nextPlayers.map(purl => playerFetch(purl))),
      playerFetch(ws.currentPlayer),
      Promise.all(ws.players.map(purl => playerFetch(purl))),
      Util.Retrieve.opt(playerStateFetch)(ws.playerState)
    ).then(t => { 
      return new Model.ActiveGameState(ws.self, t[0], t[1], t[2], t[3], ws.isLastHandTurn, ws.isLastGameTurn, t[4], t[5], ws.deckCardsNumber)
    })
  
  export const droppedGameState: Retrieve<Model.Ws.DroppedGameState, Model.DroppedGameState> = ws => 
    Util.TPromise.all4(
      card(ws.briscolaCard),
      Promise.all(ws.moves.map(m => move(m))),
      Promise.all(ws.nextPlayers.map(purl => playerFetch(purl))),
      dropReason(ws.dropReason)
    ).then(t => { 
      return new Model.DroppedGameState(ws.self, t[0], t[1], t[2], t[3])
    })

  
  export const finalGameState: Retrieve<Model.Ws.FinalGameState, Model.FinalGameState> = ws => 
    Util.TPromise.all3(
      card(ws.briscolaCard),
      Promise.all(ws.playersOrderByPoints.map(p => playerFinalState(p))),
      playerFinalState(ws.winner)).then(t => new Model.FinalGameState(ws.self, t[0], t[1], t[2]))

  const cardPlayed: Retrieve<Model.Ws.CardPlayed, Model.CardPlayed> = ws => 
    Util.TPromise.all3(
      gameFetch(ws.game),
      playerFetch(ws.player),
      card(ws.card)).then(t => new Model.CardPlayed(t[0], t[1], t[2]))

  const gameStarted: Retrieve<Model.Ws.GameStarted, Model.GameStarted> = ws => activeGameState(ws.game).then(gm => 
    new Model.GameStarted(gm)
  );
  
  const gameDropped: Retrieve<Model.Ws.GameDropped, Model.GameDropped> = ws => 
    Util.TPromise.all2(
      gameState(ws.game),
      dropReason(ws.reason)
    ).then( t => new Model.GameDropped(t[0], t[1]))

  export const briscolaEvent: Retrieve<Model.Ws.BriscolaEvent, Model.BriscolaEvent> = ws => {
    switch (BriscolaEventKind[ws.kind]) {
      case BriscolaEventKind.gameStarted: return gameStarted(<any>ws)
      case BriscolaEventKind.cardPlayed: return cardPlayed(<any>ws)
      case BriscolaEventKind.gameDropped: return gameDropped(<any>ws)
    }
  }
  
  const playerLogOn: Retrieve<Model.Ws.PlayerLogOn, Model.PlayerLogOn> = ws => playerFetch(ws.player).then(pl => 
    new Model.PlayerLogOn(pl)
  );
  
  const playerLogOff: Retrieve<Model.Ws.PlayerLogOff, Model.PlayerLogOff> = ws => playerFetch(ws.player).then(pl => 
    new Model.PlayerLogOff(pl)
  );
  
  export const playerEvent: Retrieve<Model.Ws.PlayerEvent, Model.PlayerEvent> = ws => {
    switch (PlayerEventKind[ws.kind]) {
      case PlayerEventKind.playerLogOn: return playerLogOn(<any>ws)
      case PlayerEventKind.playerLogOff: return playerLogOff(<any>ws)
    }
  }

  const numberOfGamesMatchKind: Retrieve<Model.Ws.NumberOfGamesMatchKind, Model.NumberOfGamesMatchKind> = ws =>
    Promise.resolve({ kind: MatchKindKind.numberOfGamesMatchKind, numberOfMatches: ws.numberOfMatches })


  const targetPointsMatchKind: Retrieve<Model.Ws.TargetPointsMatchKind, Model.TargetPointsMatchKind> = ws =>
    Promise.resolve({ kind: MatchKindKind.targetPointsMatchKind, winnerPoints: ws.winnerPoints })
  
  const singleMatch: Retrieve<Model.Ws.SingleMatch, Model.SingleMatch> = ws =>
    Promise.resolve({ kind: MatchKindKind.singleMatch })
  
  export const matchKind: Retrieve<Model.Ws.MatchKind, Model.MatchKind> = ws => {
    switch (MatchKindKind[ws.kind]) {
      case MatchKindKind.singleMatch: return singleMatch(<any>ws)
      case MatchKindKind.numberOfGamesMatchKind: return numberOfGamesMatchKind(<any>ws)
      case MatchKindKind.targetPointsMatchKind: return targetPointsMatchKind(<any>ws)
    }
  }

  const onPlayerCount: Retrieve<Model.Ws.OnPlayerCount, Model.OnPlayerCount> = ws => 
    Promise.resolve({ count: ws.count, kind: Model.CompetitionStartDeadlineKind.onPlayerCount })

   const allPlayers: Retrieve<Model.Ws.AllPlayers, Model.AllPlayers> = ws => 
    Promise.resolve({ kind: Model.CompetitionStartDeadlineKind.allPlayers })
  
  export const competitionStartDeadline: Retrieve<Model.Ws.CompetitionStartDeadline, Model.CompetitionStartDeadline> = ws => {
    switch (ws.kind) {
      case CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.allPlayers] : return allPlayers(ws);  
      case CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.onPlayerCount] : return onPlayerCount(<any>ws);  
    }
  }

  export const competition: Retrieve<Model.Ws.Competition, Model.Competition> = ws => 
    Util.TPromise.all3(
      Promise.all(ws.players.map(p => playerFetch(p))),
      matchKind(ws.kind),
      competitionStartDeadline(ws.deadline)).then(t => {
        return {
          players: t[0],
          kind: t[1],
          deadline: t[2]
        }
      })

  export const competitionState: Retrieve<Model.Ws.CompetitionState, Model.CompetitionState> = ws => 
    Util.TPromise.all3(
      competition(ws.competition),
      Promise.all(ws.acceptingPlayers.map(p => playerFetch(p))),
      Promise.all(ws.decliningPlayers.map(p => playerFetch(p)))).then(t => 
        new Model.CompetitionState(ws.self, CompetitionStateKind[ws.kind],  t[0], t[1], t[2], Std.option(ws.accept), Std.option(ws.decline)))      

  const competitionDeclined: Retrieve<Model.Ws.CompetitionDeclined, Model.CompetitionDeclined> = ws => 
    Util.TPromise.all2(
      playerFetch(ws.player),
      competitionFetch(ws.competition)).then(t => new Model.CompetitionDeclined(t[0], t[1], Std.option(ws.reason))) 
      

  const competitionAccepted: Retrieve<Model.Ws.CompetitionAccepted, Model.CompetitionAccepted> = ws => 
    Util.TPromise.all2(
      playerFetch(ws.player),
      competitionFetch(ws.competition)).then(t => new Model.CompetitionAccepted(t[0], t[1])) 

  const confirmedCompetition: Retrieve<Model.Ws.ConfirmedCompetition, Model.ConfirmedCompetition> = ws => 
    competitionFetch(ws.competition).then(t => new Model.ConfirmedCompetition(t)) 

  const createdCompetition: Retrieve<Model.Ws.CreatedCompetition, Model.CreatedCompetition> = ws => 
    Util.TPromise.all2(
      playerFetch(ws.issuer),
      competitionFetch(ws.competition)).then(t => new Model.CreatedCompetition(t[0], t[1])) 

  export const competitionEvent: Retrieve<Model.Ws.CompetitionEvent, Model.CompetitionEvent> = ws => {
    switch (CompetitionEventKind[ws.kind]) {
      case CompetitionEventKind.createdCompetition: return createdCompetition(<any>ws)
      case CompetitionEventKind.confirmedCompetition: return confirmedCompetition(<any>ws)
      case CompetitionEventKind.playerAccepted: return competitionAccepted(<any>ws)
      case CompetitionEventKind.playerDeclined: return competitionDeclined(<any>ws)
    }
  }
  
  export const domainEvent:Retrieve<Model.Ws.DomainEvent, Model.BriscolaEvent | Model.CompetitionEvent | Model.PlayerEvent> = ws => {
    switch (ws.kind) {
      case BriscolaEventKind[BriscolaEventKind.cardPlayed]:  
      case BriscolaEventKind[BriscolaEventKind.gameStarted]:
        return briscolaEvent(<any>ws)  
        
      case CompetitionEventKind[CompetitionEventKind.createdCompetition]:  
      case CompetitionEventKind[CompetitionEventKind.confirmedCompetition]:  
      case CompetitionEventKind[CompetitionEventKind.playerAccepted]:  
      case CompetitionEventKind[CompetitionEventKind.playerDeclined]:  
        return competitionEvent(<any>ws)  
        
      case PlayerEventKind[PlayerEventKind.playerLogOn]:  
      case PlayerEventKind[PlayerEventKind.playerLogOff]:  
        return playerEvent(<any>ws)    
        
      default:
        return Promise.reject(`unrecognized event kind ${ws.kind}`)  
    }
  } 
 
  export const gameEventAndState:Retrieve<Model.Ws.EventAndState<Model.Ws.BriscolaEvent,Model.Ws.GameState>, Model.Ws.EventAndState<Model.BriscolaEvent,Model.GameState>> = ws => 
    Util.TPromise.all2(
      briscolaEvent(ws.event),
      gameState(ws.state)).then(t => {
        return {
          event:t[0],
          state:t[1]
        }
      })
  
  export const competitionEventAndState:
    Retrieve<Model.Ws.EventAndState<Model.Ws.CompetitionEvent,Model.Ws.CompetitionState>, Model.Ws.EventAndState<Model.CompetitionEvent,Model.CompetitionState>> = ws => 
    Util.TPromise.all2(
      competitionEvent(ws.event),
      competitionState(ws.state)).then(t => {
        return {
          event:t[0],
          state:t[1]
        }
      })
  
  export const playerEventAndState:
    Retrieve<Model.Ws.EventAndState<Model.Ws.PlayerEvent,Model.Ws.Player[]>, Model.Ws.EventAndState<Model.PlayerEvent,Model.Player[]>> = ws => 
    playerEvent(ws.event).then(t => {
        return {
          event:t[0],
          state:ws.state
        }
      })
}