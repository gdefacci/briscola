import {gameStateChoice, GameState, Card, ActiveGameState, DropReason, PlayerLeft} from "./game"
import {link, convert, mapping, ChoiceValue, JsConstructor, Lazy} from "nrest-fetch"
import {DomainEvent} from "./model"
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
  @convert(mapping(ActiveGameState))
  game: ActiveGameState
}

export class GameDropped implements DomainEvent {
  get eventName() {
     return BriscolaEventKind[BriscolaEventKind.gameDropped]
  }
  @link(gameStateChoice)
  game: GameState
  @convert(mapping(PlayerLeft))
  dropReason:DropReason
}

export const gameEvents:[(a:any) => boolean, () => JsConstructor<any>][] = [[
    a => a.kind === BriscolaEventKind[BriscolaEventKind.gameStarted],
    () => GameStarted
  ], [
    a => a.kind === BriscolaEventKind[BriscolaEventKind.cardPlayed],
    () => CardPlayed
  ], [
    a => a.kind === BriscolaEventKind[BriscolaEventKind.gameDropped],
    () => GameDropped
  ]]

export const briscolaEventChoice:() => ChoiceValue<any> = Lazy.choose("GameEvent", ... gameEvents)
