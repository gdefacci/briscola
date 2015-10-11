namespace Model.Ws {

  export interface Card {
    number: number
    seed: string
  }
  
  export type DropReason = PlayerLeft
  
  export interface PlayerLeft {
    player:Path
    reason?:string
    kind:string
  }

  export type BriscolaEvent = CardPlayed | GameStarted 

  export interface CardPlayed {
    game: Path
    player: Path
    card: Card
    kind: string
  }

  export interface GameStarted {
    game: ActiveGameState
    kind: string
  }
  
  export interface GameDropped {
    game: ActiveGameState
    reason: DropReason
    kind: string
  }
  
  export type GameState = EmptyGameState | FinalGameState | ActiveGameState

  export interface EmptyGameState extends State {
    kind: string
  }

  export interface FinalGameState extends State {
    self: Path
    kind: string
    briscolaCard: Card
    playersOrderByPoints: PlayerFinalState[]
    winner: PlayerFinalState
  }

  export interface ActiveGameState extends State {
    self: Path
    kind: string
    briscolaCard: Card
    moves: Move[]
    nextPlayers: Path[]
    currentPlayer: Path
    isLastHandTurn: Boolean
    isLastGameTurn: Boolean
    players: Path[]
    playerState?: Path
    deckCardsNumber: number
  }
  
  export interface DroppedGameState {
    self:Path
    kind: string
    briscolaCard:Card
    moves:Move[] 
    nextPlayers:Path[]
    dropReason:DropReason
  }

  export interface Move {
    player: Path
    card: Card
  }

  export interface PlayerFinalState {
    player: Path
    points: number
    score: Card[]
  }

  export interface PlayerState {
    player: Path
    cards: Card[]
    score: Card[]
  }

  const gameEvents: Std.JsMap<boolean> = {
    gameStarted: true,
    cardPlayed: true,
    gameDropped: true
  }

  export function isGameEvent(str: string) {
    return gameEvents[str] === true;
  }
  
}