import {Observable} from '@reactivex/rxjs';

import * as Util from "./Util"
import {HttpClient} from "./Http"
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
  gamesChannel: Observable<GameEventAndState>
  competitionsChannel: Observable<CompetitionEventAndState>
  playersChannel: Observable<PlayerEventAndState>
  eventsLog: Observable<BriscolaEvent | CompetitionEvent | PlayerEvent>

  constructor(private resourceFetch: ResourceFetch, private http:HttpClient, webSocketFactory:(url: string) => Observable<MessageEvent>, public player: CurrentPlayer) {
    const webSocket: Observable<GameEventAndState | CompetitionEventAndState | PlayerEventAndState> = webSocketFactory(player.webSocket).flatMap((msgEv: MessageEvent) => {
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
    return this.http.POST<Input.Competition>(this.player.createCompetition, {
      players: players,
      kind: kind,
      deadline: deadlineKind
    }).then(p => this.resourceFetch.fetchObject(p, mapping(CompetitionState)))
  }

  playCard(gameState: ActiveGameState, mcard: Card): Option<Promise<GameState>> {

    const card = Input.card(mcard)
    const url: Option<Path> = gameState.playerState.map(ps => ps.self)

    return url.map(url => {
      return this.http.POST<Input.Card>(url, {
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
      this.http.POST<void>(url).then(resp =>
        resp.json().then(ws => this.resourceFetch.fetchObject(ws, mapping(CompetitionState)))))
  }

  declineCompetition(cs: CompetitionState): Option<Promise<CompetitionState>> {
    return cs.decline.map(url =>
      this.http.POST<void>(url).then(resp =>
        resp.json().then(ws => this.resourceFetch.fetchObject(ws, mapping(CompetitionState)))))
  }

}