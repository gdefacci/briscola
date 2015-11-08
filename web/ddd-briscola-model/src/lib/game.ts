import {lazy, Option, JsMap} from "flib"
import {Path, DomainEvent, byKindChoice, ByKindChoice} from "./model"
import {CurrentPlayer, Player} from "./player"
import {link, convert, Converter, ConstructorType, SimpleConverter, ByPropertyChooseConverter, ChooseConverter} from "rest-fetch"

export enum GameStateKind {
  active, dropped, finished
}

export enum Seed {
  bastoni, coppe, denari, spade
}

export enum DropReasonKind {
  playerLeft
}

const stringToSeed = SimpleConverter.fromString.andThen(Converter.toEnum(Seed, "Seed"))

export class Card {
  @convert(stringToSeed)
  seed: Seed
  "number": number
  points:number
}

export class Move {
  @link()
  player: Player

  @convert()
  card: Card
}


export class PlayerScore {
  @convert({ arrayOf:Card })
  cards: Card[]
}

export class PlayerFinalState {
  @link()
  player: Player

  points: number

  @convert()
  score: PlayerScore
}

export class PlayerState {

  @convert(Converter.propertyUrl)
  self: Path

  @link()
  player: Player

  @convert({ arrayOf:Card })
  cards: Card[]

  @convert()
  score: PlayerScore
}

export type GameState = FinalGameState | ActiveGameState | DroppedGameState

export class FinalGameState {
  self: Path

  @convert()
  briscolaCard: Card

  @convert({ arrayOf:PlayerFinalState })
  playersOrderByPoints: PlayerFinalState[]

  @convert()
  winner: PlayerFinalState
}

export class ActiveGameState {
  self: Path
  @convert()
  briscolaCard: Card
  @convert({ arrayOf:Move })
  moves: Move[]

  @link({ arrayOf:Player })
  nextPlayers: Player[]
  @link()
  currentPlayer: CurrentPlayer

  isLastHandTurn: Boolean
  isLastGameTurn: Boolean

  @link({arrayOf:Player})
  players: Player[]

  @link({optionOf:PlayerState})
  playerState: Option<PlayerState>

  deckCardsNumber: number
}

export class DropReason {
}

export const dropReasonChoice = new ChooseConverter( ws => Option.some(PlayerLeft) )

export class PlayerLeft extends DropReason {
  @link()
  player:Player

  @convert(SimpleConverter.optional(SimpleConverter.fromString))
  reason:Option<string>
}

export class DroppedGameState {
  self: Path
  briscolaCard: Card
  moves: Move[]

  @link({arrayOf:Player})
  nextPlayers: Player[]

  @convert(dropReasonChoice)
  dropReason:DropReason
}

export const gameStateChoice = byKindChoice(() => [{
    key:GameStateKind[GameStateKind.active],
    value:ActiveGameState
  }, {
    key:GameStateKind[GameStateKind.dropped],
    value:DroppedGameState
  }, {
    key:GameStateKind[GameStateKind.finished],
    value:FinalGameState
  }], "game state choice")

export module GameState {
  export function fold<T>(p: GameState,
    activeGameState: (p:ActiveGameState) => T,
    finalGameState: (p:FinalGameState) => T,
    droppedGameState: (p:DroppedGameState) => T ):T {
      if (p instanceof ActiveGameState) return activeGameState(p)
      else if (p instanceof FinalGameState) return finalGameState(p)
      else if (p instanceof DroppedGameState) return droppedGameState(p)
      else {
        console.log("unrecognized GameState")
        console.log(p)
        throw new Error("unrecognized GameState ")
      }
  }
}

