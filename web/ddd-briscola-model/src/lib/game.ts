import { Option, isNull } from "flib"
import { Path } from "./model"
import { CurrentPlayer, Player } from "./player"
import { arrayOfLinks, link, convert, Value, arrayOf, choose, optionalLink, mapping, ChoiceValue, Lazy } from "nrest-fetch"
import { toEnum } from "./Util"

export enum GameStateKind {
  active, dropped, finished
}

export enum Seed {
  bastoni, coppe, denari, spade
}

export enum DropReasonKind {
  playerLeft
}


const stringToSeed = Value.string().map(toEnum(Seed, "Seed"))

export class Card {
  @convert(() => stringToSeed)
  seed: Seed
  "number": number
  points: number
}

export class Move {
  @link()
  player: Player

  @convert()
  card: Card
}


export class PlayerScore {
  @convert(arrayOf(Card))
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

  @convert(Value.getUrl)
  self: Path

  @link()
  player: Player

  @convert(arrayOf(Card))
  cards: Card[]

  @convert()
  score: PlayerScore
}

export type GameState = FinalGameState | ActiveGameState | DroppedGameState

export type GameResult = PlayersGameResult | TeamsGameResult

export class PlayersGameResult {
  @convert(arrayOf(PlayerFinalState))
  playersOrderByPoints: PlayerFinalState[]

  @convert()
  winner: PlayerFinalState
}

export class TeamScore {
  teamName: string

  @convert(arrayOfLinks(Player))
  players: Player[]

  @convert(arrayOf(Card))
  cards: Card[]

  points: number
}

export class TeamsGameResult {
  @convert(arrayOf(TeamScore))
  teamsOrderByPoints: TeamScore[]

  @convert()
  winnerTeam: TeamScore
}

export class FinalGameState {
  self: Path

  @convert()
  briscolaCard: Card

  @convert(choose("GameResult", [
    (a: any) => !isNull(a.playersOrderByPoints),
    PlayersGameResult
  ], [
      (a: any) => !isNull(a.teamsOrderByPoints),
      TeamsGameResult
    ]))
  gameResult: GameResult

}

export class ActiveGameState {
  self: Path
  @convert()
  briscolaCard: Card
  @convert(arrayOf(Move))
  moves: Move[]

  @convert(arrayOfLinks(Player))
  nextPlayers: Player[]
  @link()
  currentPlayer: CurrentPlayer

  isLastHandTurn: Boolean
  isLastGameTurn: Boolean

  @convert(arrayOfLinks(Player))
  players: Player[]

  @convert(optionalLink(PlayerState))
  playerState: Option<PlayerState>

  deckCardsNumber: number
}

export class DropReason {
}

export class PlayerLeft extends DropReason {
  @link()
  player: Player

  @convert(Value.option(Value.string))
  reason: Option<string>
}

export class DroppedGameState {
  self: Path
  briscolaCard: Card
  moves: Move[]

  @convert(arrayOfLinks(Player))
  nextPlayers: Player[]

  @convert(mapping(PlayerLeft))
  dropReason: DropReason
}

export const gameStateChoice:() => ChoiceValue<any> = Lazy.choose("GameState", [
    a => a.kind === GameStateKind[GameStateKind.active],
    () => ActiveGameState
  ], [
    a => a.kind === GameStateKind[GameStateKind.dropped],
    () => DroppedGameState
  ], [
    a => a.kind === GameStateKind[GameStateKind.finished],
    () => FinalGameState
  ])

export module GameState {
  export function fold<T>(p: GameState,
    activeGameState: (p: ActiveGameState) => T,
    finalGameState: (p: FinalGameState) => T,
    droppedGameState: (p: DroppedGameState) => T): T {
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

