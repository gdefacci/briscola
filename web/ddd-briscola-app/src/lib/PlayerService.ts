import { observableWebSocket } from "./Util"

import * as Util from "./Util"
import { Option } from "flib"
import {
  Path, Input, eventAndStateChoice,
  GameEventAndState, BriscolaEvent, GameState, ActiveGameState, Card, gameStateChoice,
  CompetitionEventAndState, CompetitionEvent, CompetitionState,
  PlayerEventAndState, CurrentPlayer, PlayerEvent
} from "ddd-briscola-model"

import { ResourceFetch, mapping } from "nrest-fetch"

function asGameEventAndState(a: any): Option<GameEventAndState> {
  if (a instanceof GameEventAndState) return Option.some(a)
  else return Option.None;
}

function asCompetitionEventAndState(a: any): Option<CompetitionEventAndState> {
  if (a instanceof CompetitionEventAndState) return Option.some(a)
  else return Option.None;
}

function asPlayerEventAndState(a: any): Option<PlayerEventAndState> {
  if (a instanceof PlayerEventAndState) return Option.some(a)
  else return Option.None;
}

export class PlayerService {
  gamesChannel: Rx.Observable<GameEventAndState>
  competitionsChannel: Rx.Observable<CompetitionEventAndState>
  playersChannel: Rx.Observable<PlayerEventAndState>
  eventsLog: Rx.Observable<BriscolaEvent | CompetitionEvent | PlayerEvent>

  constructor(private resourceFetch: ResourceFetch, public player: CurrentPlayer) {
    const webSocket: Rx.Observable<GameEventAndState | CompetitionEventAndState | PlayerEventAndState> = observableWebSocket(player.webSocket).flatMap((msgEv: MessageEvent) => {
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
    this.playersChannel = Util.rxCollect(webSocket, asPlayerEventAndState)
    this.eventsLog = webSocket.map(es => es.event)
  }

  createCompetition(players: Path[], kind: Input.MatchKind, deadlineKind: Input.CompetitionStartDeadline): Promise<CompetitionState> {
    return Util.Http.POST<Input.Competition>(this.player.createCompetition, {
      players: players,
      kind: kind,
      deadline: deadlineKind
    }).then(p => this.resourceFetch.fetchObject(p, mapping(CompetitionState)))
  }

  playCard(gameState: ActiveGameState, mcard: Card): Option<Promise<GameState>> {

    const card = Input.card(mcard)
    const url: Option<Path> = gameState.playerState.map(ps => ps.self)

    return url.map(url => {
      return Util.Http.POST<Input.Card>(url, {
        "number": card.number,
        seed: card.seed
      }).then(resp => {
        return resp.json().then(ws => {
          return this.resourceFetch.fetchObject(ws, gameStateChoice)
        })
      })
    })
  }

  acceptCompetition(cs: CompetitionState): Option<Promise<CompetitionState>> {
    return cs.accept.map(url =>
      Util.Http.POST<void>(url).then(resp =>
        resp.json().then(ws => this.resourceFetch.fetchObject(ws, mapping(CompetitionState)))))
  }

  declineCompetition(cs: CompetitionState): Option<Promise<CompetitionState>> {
    return cs.decline.map(url =>
      Util.Http.POST<void>(url).then(resp =>
        resp.json().then(ws => this.resourceFetch.fetchObject(ws, mapping(CompetitionState)))))
  }

}