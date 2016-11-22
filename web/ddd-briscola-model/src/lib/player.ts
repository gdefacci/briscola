import { DomainEvent, Path } from "./model"
import { link, arrayOfLinks, convert, ChoiceValue, JsConstructor, Lazy } from "nrest-fetch"

export class Player {
  self: Path
  name: string
}

export class CurrentPlayer extends Player {
  webSocket: Path
  createCompetition: Path
}

export enum PlayerEventKind {
  playerLogOn, playerLogOff
}

export class Team {
  self: Path
  name: string
  @convert(arrayOfLinks(Player))
  players: Player[]
}

export type PlayerEvent = PlayerLogOn | PlayerLogOff

export class PlayerLogOn implements DomainEvent {
  get eventName() {
    return PlayerEventKind[PlayerEventKind.playerLogOn]
  }
  @link()
  player: Player
}

export class PlayerLogOff implements DomainEvent {
  get eventName() {
    return PlayerEventKind[PlayerEventKind.playerLogOff]
  }
  @link()
  player: Player
}

export const playerEvents:[(a:any) => boolean, () => JsConstructor<any>][] = [
  [
    a => a.kind === PlayerEventKind[PlayerEventKind.playerLogOn],
    () => PlayerLogOn
  ], [
    a => a.kind === PlayerEventKind[PlayerEventKind.playerLogOff],
    () => PlayerLogOff
  ]
]

export const playerEventChoice: () => ChoiceValue<any> = Lazy.choose("PlayerEvent", ... playerEvents)

export class PlayersCollection {
  @convert(arrayOfLinks(Player))
  members: Player[]
}