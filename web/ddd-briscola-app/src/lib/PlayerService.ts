import {observableWebSocket} from "./Util"

import * as Util from "./Util"
import {Option, JsMap} from "flib"
import {DomainEvent, Path, Input, eventAndStateChoice,
  GameEventAndState, BriscolaEvent, GameState, BriscolaEventKind, ActiveGameState, FinalGameState, GameStarted, Card, gameStateChoice,
  CompetitionEventAndState, CompetitionEvent, CompetitionState, CompetitionEventKind, CompetitionStateKind,
  PlayerEventAndState, CurrentPlayer, Player, PlayerEvent, PlayerEventKind} from "ddd-briscola-model"

import {fetch, fetchChoose, ConstructorType} from "rest-fetch"

function gamesMap(ch: Rx.Observable<GameEventAndState>): (p: Path) => Option<GameState> {
  const mapOfGames: JsMap<GameState> = {}
  const feedGamesMap = (gm: GameState) => {
    if (gm instanceof ActiveGameState) {
      mapOfGames[gm.self] = gm
    } else if (gm instanceof FinalGameState) {
      mapOfGames[gm.self] = gm
    }
  }
  ch.subscribe(es => feedGamesMap(es.game));
  return (p) => Option.option(mapOfGames[p])
}

function competitionsMap(ch: Rx.Observable<CompetitionEventAndState>): (p: Path) => Option<CompetitionState> {
  const compMap: JsMap<CompetitionState> = {}
  ch.subscribe(es => {
    const compState = es.competition;
    if (es.competition.kind === CompetitionStateKind.open) {
      compMap[compState.self] = compState;
    } else {
      delete compMap[compState.self];
    }
  });
  return (p) => Option.option(compMap[p])
}

function asGameEventAndState(a:any):Option<GameEventAndState> {
  if (a instanceof GameEventAndState) return Option.some(a)
  else return Option.none<GameEventAndState>();
}

function asCompetitionEventAndState(a:any):Option<CompetitionEventAndState> {
  if (a instanceof CompetitionEventAndState) return Option.some(a)
  else return Option.none<CompetitionEventAndState>();
}

function asPlayerEventAndState(a:any):Option<PlayerEventAndState> {
  if (a instanceof PlayerEventAndState) return Option.some(a)
  else return Option.none<PlayerEventAndState>();
}

export class PlayerService {
  gamesChannel: Rx.Observable<GameEventAndState>
  competitionsChannel: Rx.Observable<CompetitionEventAndState>
  playersChannel: Rx.Observable<PlayerEventAndState>
  eventsLog: Rx.Observable<BriscolaEvent | CompetitionEvent | PlayerEvent>

  private gamesMap: (p: Path) => Option<GameState>
  private competitionsMap: (p: Path) => Option<CompetitionState>

	constructor(public player:CurrentPlayer) {
	  const webSocket:Rx.Observable<GameEventAndState | CompetitionEventAndState | PlayerEventAndState> = observableWebSocket(player.webSocket).flatMap((msgEv: MessageEvent) => {
      const data = msgEv.data
      if (typeof data === "string") {
        const msg = JSON.parse(data)
        return fetchChoose(eventAndStateChoice).fromObject(msg).then(
          (v) => Promise.resolve(v),
          err => {
            console.log("error parsing web socket message")
            console.log(`error ${err}`)
            console.log(err.stack)
            console.log(data)
            return Promise.reject(`error fetching event and state ${data}, error : ${err}`)
          }
        )
      } else {
        console.log("Error")
        console.log("msgEv.data:")
        console.log(data)
        return Promise.reject("expecting string")
      }
    })

    this.gamesChannel = Util.rxCollect(webSocket, asGameEventAndState)
    this.competitionsChannel = Util.rxCollect(webSocket, asCompetitionEventAndState)
    this.playersChannel  = Util.rxCollect(webSocket, asPlayerEventAndState)
    this.eventsLog = webSocket.map( es => es.event )

    this.gamesMap = gamesMap(this.gamesChannel);
    this.competitionsMap = competitionsMap(this.competitionsChannel);
	}

  createCompetition(players: Path[], kind: Input.MatchKind, deadlineKind: Input.CompetitionStartDeadline): Promise<CompetitionState> {
    return Util.Http.POST<Input.Competition>(this.player.createCompetition, {
      players: players,
      kind: kind,
      deadline: deadlineKind
    }).then(p => fetch(CompetitionState).fromObject(p))
  }

  gameChannelById(gameSelf: Path): Rx.Observable<GameEventAndState> {
    return this.gamesChannel.filter(es =>
      (es.game  instanceof ActiveGameState && es.game.self === gameSelf) ||
      (es.game instanceof FinalGameState && es.game.self === gameSelf)
    )
  }

  playCard(gameSelf: Path, mcard: Card): Option<Promise<GameState>> {
    function playerStateUrl(gm: GameState): Option<Path> {
      if (gm instanceof ActiveGameState) {
        return gm.playerState.map(ps => ps.self)
      } else {
        return Option.none<Path>();
      }
    }
    const card = Input.card(mcard)
    return this.gamesMap(gameSelf).flatMap(gm => {
      const url: Option<Path> = playerStateUrl(gm)

      return url.map(url => {
        return Util.Http.POST<Input.Card>(url, {
          "number": card.number,
          seed: card.seed
        }).then(resp => {
          return resp.json().then( ws => {
            return fetchChoose(gameStateChoice).fromObject(ws)
          })
        })
      })
    })
  }

  acceptCompetition(compSelf: Path): Option<Promise<CompetitionState>> {
    return this.competitionsMap(compSelf).flatMap(cs =>
      cs.accept.map(url =>
        Util.Http.POST<void>(url).then(resp =>
          resp.json().then( ws =>fetch(CompetitionState).fromObject(ws) )
        )
      )
    )
  }

  declineCompetition(compSelf: Path): Option<Promise<CompetitionState>> {
    return this.competitionsMap(compSelf).flatMap(cs =>
      cs.decline.map(url =>
        Util.Http.POST<void>(url).then(resp =>
          resp.json().then( ws =>
            fetch(CompetitionState).fromObject(ws)
          )
        )
      )
    )
  }

}