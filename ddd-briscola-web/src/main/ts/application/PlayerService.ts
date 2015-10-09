namespace Application {

  import Path = Model.Ws.Path
  import EventAndState = Model.Ws.EventAndState
  import CompetitionState = Model.Ws.CompetitionState
  import GameState = Model.Ws.GameState
  import BriscolaEvent = Model.Ws.BriscolaEvent
  import CompetitionEvent = Model.Ws.CompetitionEvent
  import SiteMap = Model.Ws.SiteMap
  import DomainEvent = Model.Ws.DomainEvent
  import State = Model.Ws.State
  import Option = Std.Option

  const Fetch = Util.Fetch

  export class PlayersService {
    constructor(private siteMap: SiteMap) {
    }
    allPlayers() {
      return Fetch.GET<Model.Ws.Collection<Model.Ws.Player>>(this.siteMap.players)
    }
    player(playerSelf: string) {
      return Fetch.GET<Model.Ws.Collection<Model.Ws.Player>>(playerSelf)
    }
    createPlayer(name: string, password:string) {
      return Fetch.POST<Model.Input.Player, Model.Ws.Player>(this.siteMap.players, {
        name: name,
        password:password
      })
    }
    logon(name: string, password:string) {
      return Fetch.POST<Model.Input.Player, Model.Ws.Player>(this.siteMap.playerLogin, {
        name: name,
        password:password
      })
    }
  }

  function gamesMap(ch: Rx.Observable<EventAndState<Model.BriscolaEvent, Model.GameState>>): (p: Path) => Option<Model.GameState> {
    const gamesMap: Std.JsMap<Model.GameState> = {}
    const feedGamesMap = (gm: Model.GameState) => {
      if (gm instanceof Model.ActiveGameState) {
        gamesMap[gm.self] = gm
      } else if (gm instanceof Model.FinalGameState) {
        gamesMap[gm.self] = gm
      }
    }
    ch.subscribe(es => feedGamesMap(es.state));
    return Std.optStringMapping(gamesMap)
  }

  function competitionsMap(ch: Rx.Observable<EventAndState<Model.CompetitionEvent, Model.CompetitionState>>): (p: Path) => Option<Model.CompetitionState> {
    const compMap: Std.JsMap<Model.CompetitionState> = {}
    ch.subscribe(es => {

      const compState = es.state;
      if (es.state.kind === Model.CompetitionStateKind.open) {
        compMap[compState.self] = compState;
      } else {
        delete compMap[compState.self];
      }

    });

    return Std.optStringMapping(compMap)
  }


  export class PlayerWsService {
    gamesChannel: Rx.Observable<EventAndState<BriscolaEvent, GameState>>
    competitionsChannel: Rx.Observable<EventAndState<CompetitionEvent, CompetitionState>>
    playersChannel: Rx.Observable<EventAndState<Model.Ws.PlayerEvent, Model.Ws.Player[]>>
    eventsLog: Rx.Observable<DomainEvent>

    constructor(public player: Model.Ws.Player) {
      Util.assert(!Std.isNull(player.webSocket), "null webSocket")
      Util.assert(!Std.isNull(player.createCompetition), "null createCompetition")
      const webSocket = Util.RxUtil.webSocketObservable(player.webSocket).flatMap((msgEv: MessageEvent) => {
        const data = msgEv.data
        if (typeof data === "string") {
            const es = <Model.Ws.EventAndState<DomainEvent, State>>JSON.parse(data);
            Util.assert(!Std.isNull(es.event.kind), "null event.kind")
//            Util.assert(!Std.isNull(es.state.kind), "null state.kind")
            return Rx.Observable.from([es]);
        } else {
          console.log("Error")
          console.log("msgEv.data:")
          console.log(data)
          throw new Error("expecting string")
        }
      })
      this.competitionsChannel = <any>webSocket
        .filter(es => Model.Ws.isCompetitionEvent(es.event.kind))

      this.gamesChannel = <any>webSocket.
        filter(es => Model.Ws.isGameEvent(es.event.kind))

      this.playersChannel = <any>webSocket.
        filter(es => Model.Ws.isPlayerEvent(es.event.kind))
      
      this.eventsLog = webSocket.map(es => {
        return es.event
      })
      
    }

  }

  export class PlayerService {
    player:Model.Player
    gamesChannel: Rx.Observable<EventAndState<Model.BriscolaEvent, Model.GameState>>
    competitionsChannel: Rx.Observable<EventAndState<Model.CompetitionEvent, Model.CompetitionState>>
    playersChannel: Rx.Observable<EventAndState<Model.PlayerEvent, Model.Player[]>>
    eventsLog: Rx.Observable<Model.BriscolaEvent | Model.CompetitionEvent | Model.PlayerEvent>
    private gameStartChannel: Rx.Observable<Model.ActiveGameState>
    private gameEndChannel: Rx.Observable<Model.FinalGameState>
    private gamesMap: (p: Path) => Option<Model.GameState>
    private competitionsMap: (p: Path) => Option<Model.CompetitionState>

    constructor(private playerWsService: PlayerWsService) {
      this.player = playerWsService.player
      this.gamesChannel = playerWsService.gamesChannel.flatMap(ws => {
        const r = ToModel.gameEventAndState(ws)
        return r;
      })
      this.competitionsChannel = playerWsService.competitionsChannel.flatMap(ws => ToModel.competitionEventAndState(ws))
      this.playersChannel = playerWsService.playersChannel.flatMap(ws => ToModel.playerEventAndState(ws))
      this.eventsLog = playerWsService.eventsLog.flatMap(ws => {
        return ToModel.domainEvent(ws)
      })
      this.gameStartChannel = this.eventsLog.flatMap(e => {
        if (e instanceof Model.GameStarted) {
          return Rx.Observable.from([e.game])
        } else {
          return Rx.Observable.from<Model.ActiveGameState>([])
        }
      })
      this.gameEndChannel = this.gamesChannel.flatMap(e => {
        const state = e.state
        if (state instanceof Model.FinalGameState) {
          return Rx.Observable.from([state])
        } else {
          return Rx.Observable.from<Model.FinalGameState>([])
        }
      })
      this.gamesMap = gamesMap(this.gamesChannel);
      this.competitionsMap = competitionsMap(this.competitionsChannel);
    }

    createCompetition(players: Path[], kind: Model.Input.MatchKind, deadlineKind: Model.Input.CompetitionStartDeadline): Promise<Model.CompetitionState> {
      return Fetch.POST<Model.Input.Competition, Model.Ws.CompetitionState>(this.playerWsService.player.createCompetition, {
        players: players,
        kind: kind,
        deadline: deadlineKind
      }).then(p => ToModel.competitionState(p))
    }

    gameChannelById(gameSelf: Path): Rx.Observable<EventAndState<Model.BriscolaEvent, Model.GameState>> {
      return this.gamesChannel.filter(es =>
        (es.state instanceof Model.ActiveGameState && es.state.self === gameSelf) ||
        (es.state instanceof Model.FinalGameState && es.state.self === gameSelf)
      )
    }

    playCard(gameSelf: Path, mcard: Model.Card): Option<Promise<Model.GameState>> {
      function playerStateUrl(gm: Model.GameState): Std.Option<Path> {
        if (gm instanceof Model.ActiveGameState) {
          return gm.playerState.map(ps => ps.self)
        } else {
          return Std.none<Path>();
        }
      }
      const card = ToWs.card(mcard)
      return this.gamesMap(gameSelf).flatMap(gm => {
        const url: Option<Path> = playerStateUrl(gm)

        return url.map(url => {
          return Fetch.POST<Model.Input.Card, GameState>(url, {
            "number": card.number,
            seed: card.seed
          }).then(ws => ToModel.gameState(ws))
        })
      })
    }

    acceptCompetition(compSelf: Path): Option<Promise<Model.CompetitionState>> {
      return this.competitionsMap(compSelf).flatMap(cs =>
        cs.accept.map(url =>
          Fetch.POST<void, CompetitionState>(url).then(ws => ToModel.competitionState(ws))
        )
      )
    }

    declineCompetition(compSelf: Path): Option<Promise<Model.CompetitionState>> {
      return this.competitionsMap(compSelf).flatMap(cs =>
        cs.decline.map(url =>
          Fetch.POST<void, CompetitionState>(url).then(ws => ToModel.competitionState(ws))
        )
      )
    }
  }

}