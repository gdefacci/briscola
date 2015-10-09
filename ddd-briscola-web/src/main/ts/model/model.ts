namespace Model {

  import Option = Std.Option
  import JsMap = Std.JsMap

  export type Player = Ws.Player

  export interface DomainEvent {
    eventName: string
  }

  export interface Aggregate {
    aggregateName: string
  }

  export enum CompetitionEventKind {
    createdCompetition, confirmedCompetition, playerAccepted, playerDeclined
  }

  export enum CompetitionStateKind {
    empty, open, dropped, fullfilled
  }

  export enum GameStateKind {
    empty, active, finished
  }

  export enum CompetitionStartDeadlineKind {
    onPlayerCount
  }

  export enum BriscolaEventKind {
    gameStarted, cardPlayed
  }

  export enum PlayerEventKind {
    playerLogOn, playerLogOff
  }

  export enum MatchKindKind {
    numberOfGamesMatchKind, targetPointsMatchKind
  }

  export enum Seed {
    bastoni, coppe, denari, spade
  }

  export type CompetitionEvent = CompetitionDeclined | CompetitionAccepted | ConfirmedCompetition | CreatedCompetition

  export class CompetitionDeclined implements DomainEvent {
    constructor(public player: Player,
      public competition: CompetitionState,
      public reason: Option<string>) {
    }
    eventName = CompetitionEventKind[CompetitionEventKind.playerDeclined]
  }

  export class CompetitionAccepted implements DomainEvent {
    constructor(public player: Player,
      public competition: CompetitionState) {
    }
    eventName = CompetitionEventKind[CompetitionEventKind.playerAccepted]
  }

  export class ConfirmedCompetition implements DomainEvent {
    constructor(public competition: CompetitionState) {
    }
    eventName = CompetitionEventKind[CompetitionEventKind.confirmedCompetition]
  }

  export class CreatedCompetition implements DomainEvent {
    constructor(public issuer: Player,
      public competition: CompetitionState) {
    }
    eventName = CompetitionEventKind[CompetitionEventKind.createdCompetition]
  }

  export class CompetitionState implements Aggregate {
    constructor(public self: Ws.Path,
      public kind: CompetitionStateKind,
      public competition: Competition,
      public acceptingPlayers: Player[],
      public decliningPlayers: Player[],
      public accept: Option<Ws.Path>,
      public decline: Option<Ws.Path>) {
    }
    public aggregateName = "CompetitionState"
  }

  export type CompetitionStartDeadline = string | OnPlayerCount

  export interface OnPlayerCount {
    count: number
    kind: CompetitionStartDeadlineKind
  }

  export interface Competition {
    players: Player[]
    kind: MatchKind
    deadline: CompetitionStartDeadline
  }

  export type BriscolaEvent = CardPlayed | GameStarted

  export class CardPlayed implements DomainEvent {
    constructor(public game: GameState, public player: Player, public card: Card) {
    }
    eventName = BriscolaEventKind[BriscolaEventKind.cardPlayed]
  }

  export class GameStarted implements DomainEvent {
    constructor(public game: ActiveGameState) {
    }
    eventName = BriscolaEventKind[BriscolaEventKind.gameStarted]
  }

  export type PlayerEvent = PlayerLogOn | PlayerLogOff

  export class PlayerLogOn implements DomainEvent {
    constructor(public player: Player) {
    }
    eventName = PlayerEventKind[PlayerEventKind.playerLogOn]
  }

  export class PlayerLogOff implements DomainEvent {
    constructor(public player: Player) {
    }
    eventName = PlayerEventKind[PlayerEventKind.playerLogOff]
  }

  export type MatchKind = string | NumberOfGamesMatchKind | TargetPointsMatchKind

  export interface NumberOfGamesMatchKind {
    numberOfMatches: number
    kind: MatchKindKind
  }

  export interface TargetPointsMatchKind {
    winnerPoints: number
    kind: MatchKindKind
  }

  export type GameState = FinalGameState | ActiveGameState

  export class FinalGameState implements Aggregate {
    constructor(
      public self: Ws.Path,
      public briscolaCard: Card,
      public playersOrderByPoints: PlayerFinalState[],
      public winner: PlayerFinalState) {
    }
    aggregateName = "FinalGameState"
  }

  export class ActiveGameState implements Aggregate {
    constructor(
      public self: Ws.Path,
      public briscolaCard: Card,
      public moves: Move[],
      public nextPlayers: Player[],
      public currentPlayer: Player,
      public isLastHandTurn: Boolean,
      public isLastGameTurn: Boolean,
      public players: Player[],
      public playerState: Option<PlayerState>,
      public deckCardsNumber:number) {
    }
    aggregateName = "ActiveGameState"
  }

  export interface Card {
    seed: Seed
    "number": number
  }

  export interface Move {
    player: Player
    card: Card
  }

  export interface PlayerFinalState {
    player: Player
    points: number
    score: Card[]
  }

  export interface PlayerState {
    self:Ws.Path
    player: Player
    cards: Card[]
    score: Card[]
  }


  export module BriscolaEvent {
    export function fold<T>(
      gameStarted: (p: GameStarted) => T, cardPlayed: (p: CardPlayed) => T): (p: BriscolaEvent) => T {
      return p => {
        if (p instanceof GameStarted) return gameStarted(p)
        else if (p instanceof CardPlayed) return cardPlayed(p)
        else {
          console.log("unrecognized BriscolaEvent")
          console.log(p)
          Util.fail<T>("unrecognized BriscolaEvent ")
        }
      }
    }
  }

  export module CompetitionEvent {
    export function fold<T>(
      createdCompetition: (p: CreatedCompetition) => T, competitionAccepted: (p: CompetitionAccepted) => T, competitionDeclined: (p: CompetitionDeclined) => T, confirmedCompetition: (p: ConfirmedCompetition) => T): (p: CompetitionEvent) => T {
      return p => {
        if (p instanceof CreatedCompetition) return createdCompetition(p)
        else if (p instanceof CompetitionAccepted) return competitionAccepted(p)
        else if (p instanceof CompetitionDeclined) return competitionDeclined(p)
        else if (p instanceof ConfirmedCompetition) return confirmedCompetition(p)
        else {
          console.log("unrecognized CompetitionEvent")
          console.log(p)
          Util.fail<T>("unrecognized CompetitionEvent ")
        }
      }
    }
  }


  export module PlayerEvent {
    export function fold<T>(
      playerLogOn: (p: PlayerLogOn) => T, playerLogOff: (p: PlayerLogOff) => T): (p: PlayerEvent) => T {
      return p => {
        if (p instanceof PlayerLogOn) return playerLogOn(p)
        else if (p instanceof PlayerLogOff) return playerLogOff(p)
        else {
          console.log("unrecognized PlayerEvent")
          console.log(p)
          Util.fail<T>("unrecognized PlayerEvent ")
        }
      }
    }
  }

  export module GameState {
    export function fold<T>(
      activeGameState: (p: ActiveGameState) => T, finalGameState: (p: FinalGameState) => T): (p: GameState) => T {
      return p => {
        if (p instanceof ActiveGameState) return activeGameState(p)
        else if (p instanceof FinalGameState) return finalGameState(p)
        else {
          console.log("unrecognized GameState")
          console.log(p)
          Util.fail<T>("unrecognized GameState ")
        }
      }
    }
  }


}