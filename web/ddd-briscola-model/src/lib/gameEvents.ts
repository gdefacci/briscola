import {dropReasonChoice, gameStateChoice, GameState, Card, ActiveGameState, DropReason} from "./game"
import {link, convert, ByPropertyChooseConverter} from "rest-fetch"
import {DomainEvent, byKindChoice, ByKindChoice} from "./model"
import {Player} from "./player"

export enum BriscolaEventKind {
  gameStarted, cardPlayed, gameDropped
}

export type BriscolaEvent = CardPlayed | GameStarted | GameDropped

export class CardPlayed implements DomainEvent {
  get eventName() {
     return BriscolaEventKind[BriscolaEventKind.cardPlayed]
  }

  @link(gameStateChoice)
  game: GameState
  @link()
  player: Player
  @convert()
  card: Card
}

export class GameStarted implements DomainEvent {
  get eventName() {
     return BriscolaEventKind[BriscolaEventKind.gameStarted]
  }
  @convert(ActiveGameState)
  game: ActiveGameState
}

export class GameDropped implements DomainEvent {
  get eventName() {
     return BriscolaEventKind[BriscolaEventKind.gameDropped]
  }
  @link(gameStateChoice)
  game: GameState
  @convert(dropReasonChoice)
  dropReason:DropReason
}

export const briscolaEventChoice = byKindChoice(() =>
  [{
    key:BriscolaEventKind[BriscolaEventKind.gameStarted],
    value:GameStarted
  }, {
    key:BriscolaEventKind[BriscolaEventKind.cardPlayed],
    value:CardPlayed
  }, {
    key:BriscolaEventKind[BriscolaEventKind.gameDropped],
    value:GameDropped
  }], "game event")
