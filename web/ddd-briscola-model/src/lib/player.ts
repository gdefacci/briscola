import {DomainEvent, Path, ByKindChoice, byKindChoice} from "./model"
import {Option} from "flib"
import {link, ByPropertyChooseConverter} from "rest-fetch"

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

export const playerEventChoice = byKindChoice(() =>
  [{
    key:PlayerEventKind[PlayerEventKind.playerLogOn],
    value:PlayerLogOn
  }, {
    key:PlayerEventKind[PlayerEventKind.playerLogOff],
    value:PlayerLogOff
  }], "player event")

export class PlayersCollection {
  @link({arrayOf:Player})
  members:Player[]
}