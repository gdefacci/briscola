module Model {

  import Option = Std.Option

  export enum GameStateKind {
    empty, active, dropped, finished
  }

  export enum BriscolaEventKind {
    gameStarted, cardPlayed, gameDropped
  }

  export enum Seed {
    bastoni, coppe, denari, spade
  }
  
  export enum DropReasonKind {
    playerLeft  
  }

  export type BriscolaEvent = CardPlayed | GameStarted | GameDropped 
  
  export class CardPlayed extends DomainEvent {
    constructor(public game: GameState, public player: Player, public card: Card) {
      super(BriscolaEventKind[BriscolaEventKind.cardPlayed])
    }
  }

  export class GameStarted extends DomainEvent {
    constructor(public game: ActiveGameState) {
      super(BriscolaEventKind[BriscolaEventKind.gameStarted])
    }
  }
  
  export class GameDropped extends DomainEvent {
    constructor(public game: GameState, dropReason:DropReason) {
      super(BriscolaEventKind[BriscolaEventKind.gameDropped])
    }
  }

  export type GameState = FinalGameState | ActiveGameState | DroppedGameState

  export class FinalGameState extends Aggregate {
    constructor(
      public self: Ws.Path,
      public briscolaCard: Card,
      public playersOrderByPoints: PlayerFinalState[],
      public winner: PlayerFinalState) {
      super()
    }
  }

  export class ActiveGameState extends Aggregate {
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
      public deckCardsNumber: number) {
      super()
    }
  }
  
  export class DropReason {
  }
  
  export class PlayerLeft extends DropReason {
    constructor(public player:Player, reason:Option<string>) {
      super();
    }
  }
  
  export class DroppedGameState extends Aggregate {
    constructor(
      public self: Ws.Path,
      public briscolaCard: Card,
      public moves: Move[],
      public nextPlayers: Player[],
      public dropReason:DropReason) {
      super()
    }
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
    self: Ws.Path
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
          return Util.fail<T>("unrecognized BriscolaEvent ")
        }
      }
    }
  }

  export module GameState {
    export function fold<T>(
      activeGameState: (p:ActiveGameState) => T, 
      finalGameState: (p:FinalGameState) => T,
      droppedGameState: (p:DroppedGameState) => T ): (p: GameState) => T {
      return p => {
        if (p instanceof ActiveGameState) return activeGameState(p)
        else if (p instanceof FinalGameState) return finalGameState(p)
        else if (p instanceof DroppedGameState) return droppedGameState(p)
        else {
          console.log("unrecognized GameState")
          console.log(p)
          return Util.fail<T>("unrecognized GameState ")
        }
      }
    }
  }

}