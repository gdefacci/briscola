import {observableWebSocket} from "./Util"

import * as Util from "./Util"
import {Option, JsMap} from "flib"
import {Path, Input, eventAndStateChoice,
  GameEventAndState, BriscolaEvent, GameState, ActiveGameState, FinalGameState, Card, gameStateChoice,
  CompetitionEventAndState, CompetitionEvent, CompetitionState, CompetitionStateKind,
  PlayerEventAndState, CurrentPlayer, PlayerEvent} from "ddd-briscola-model"

import {ResourceFetch, mapping} from "nrest-fetch"

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
  else return Option.None;
}

function asCompetitionEventAndState(a:any):Option<CompetitionEventAndState> {
  if (a instanceof CompetitionEventAndState) return Option.some(a)
  else return Option.None;
}

function asPlayerEventAndState(a:any):Option<PlayerEventAndState> {
  if (a instanceof PlayerEventAndState) return Option.some(a)
  else return Option.None;
}

export class PlayerService {
  gamesChannel: Rx.Observable<GameEventAndState>
  competitionsChannel: Rx.Observable<CompetitionEventAndState>
  playersChannel: Rx.Observable<PlayerEventAndState>
  eventsLog: Rx.Observable<BriscolaEvent | CompetitionEvent | PlayerEvent>

  private gamesMap: (p: Path) => Option<GameState>
  private competitionsMap: (p: Path) => Option<CompetitionState>

	constructor(private resourceFetch:ResourceFetch, public player:CurrentPlayer) {
	  const webSocket:Rx.Observable<GameEventAndState | CompetitionEventAndState | PlayerEventAndState> = observableWebSocket(player.webSocket).flatMap((msgEv: MessageEvent) => {
      const data = msgEv.data
      if (typeof data === "string") {
        const msg = JSON.parse(data)
        return resourceFetch.fetchObject(msg, eventAndStateChoice).then(
          (v) => Promise.resolve(v),
          err => {
            console.log("error parsing web socket message")
            console.log(`error ${err}`)
            if (err && err.stack) console.log(err.stack)
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
    }).then(p => this.resourceFetch.fetchObject(p, mapping(CompetitionState)))
  }

  /*gameChannelById(gameSelf: Path): Rx.Observable<GameEventAndState> {
    return this.gamesChannel.filter(es =>
      (es.game  instanceof ActiveGameState && es.game.self === gameSelf) ||
      (es.game instanceof FinalGameState && es.game.self === gameSelf)
    )
  }*/

  playCard(gameSelf: Path, mcard: Card): Option<Promise<GameState>> {
    function playerStateUrl(gm: GameState): Option<Path> {
      if (gm instanceof ActiveGameState) {
        return gm.playerState.map(ps => ps.self)
      } else {
        return Option.None;
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
            return this.resourceFetch.fetchObject(ws, gameStateChoice)
          })
        })
      })
    })
  }

  acceptCompetition(compSelf: Path): Option<Promise<CompetitionState>> {
    return this.competitionsMap(compSelf).flatMap(cs =>
      cs.accept.map(url =>
        Util.Http.POST<void>(url).then(resp =>
          resp.json().then( ws => this.resourceFetch.fetchObject(ws, mapping(CompetitionState)) )
        )
      )
    )
  }

  declineCompetition(compSelf: Path): Option<Promise<CompetitionState>> {
    return this.competitionsMap(compSelf).flatMap(cs =>
      cs.decline.map(url =>
        Util.Http.POST<void>(url).then(resp =>
          resp.json().then( ws =>
            this.resourceFetch.fetchObject(ws, mapping(CompetitionState))
          )
        )
      )
    )
  }

}