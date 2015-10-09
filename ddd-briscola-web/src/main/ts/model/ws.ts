namespace Model.Ws {
  
  export type Path = string

  export interface EventAndState<E, S> {
    event: E
    state: S
  }
  
  export interface Collection<T> {
    members:T[]
  }
  
  export interface DomainEvent {
    kind:string
  }
  
  export interface State {
  }

  export type CompetitionEvent = CompetitionDeclined | CompetitionAccepted | ConfirmedCompetition | CreatedCompetition
  
  export interface CompetitionDeclined extends DomainEvent {
    player: Path
    competition: Path
    reason?: string
  }

  export interface CompetitionAccepted extends DomainEvent {
    player: Path
    competition: Path
  }

  export interface ConfirmedCompetition extends DomainEvent {
    competition: Path
  }

  export interface CreatedCompetition extends DomainEvent {
    issuer: Path
    competition: Path
  }
  
  export interface Card {
    number: number 
    seed: string
  }

  export interface CompetitionState extends State {
    self?: Path
    kind:string
    competition?: Competition
    acceptingPlayers: Path[]
    decliningPlayers: Path[]
    accept?: Path
    decline?: Path
  }

  export type CompetitionStartDeadline = string | OnPlayerCount 
  
  export interface OnPlayerCount {
    count:number
    kind:string
  }

  export interface Competition {
    players: Path[]
    kind: MatchKind
    deadline: CompetitionStartDeadline
  }

  export type BriscolaEvent =  CardPlayed | GameStarted
  
  export interface CardPlayed {
    game: Path
    player: Path
    card: Card
    kind:string
  }

  export interface GameStarted {
    game: ActiveGameState
    kind:string
  }

  export type PlayerEvent = PlayerLogOn | PlayerLogOff
  
  export interface PlayerLogOn {
    player: Path
    kind:string
  }
  
  export interface PlayerLogOff {
    player: Path
    kind:string
  }
  
  export type MatchKind = string | NumberOfGamesMatchKind | TargetPointsMatchKind
  
  export interface NumberOfGamesMatchKind {
    numberOfMatches:number
    kind:string
  }
  
  export interface TargetPointsMatchKind {
    winnerPoints:number
    kind:string
  }

  export type GameState = EmptyGameState | FinalGameState | ActiveGameState
  
  export interface EmptyGameState extends State {
    kind:string
  }
  
  export interface FinalGameState extends State {
    self: Path
    kind:string
    briscolaCard:Card
    playersOrderByPoints: PlayerFinalState[]
    winner: PlayerFinalState
  }

  export interface ActiveGameState extends State {
    self: Path
    kind:string
    briscolaCard:Card
    moves: Move[]
    nextPlayers: Path[]
    currentPlayer: Path
    isLastHandTurn: Boolean
    isLastGameTurn: Boolean
    players: Path[]
    playerState?: Path
    deckCardsNumber:number
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

  export interface Player {
    self: Path
    name: string
    webSocket?: Path
    createCompetition?: Path
  }

  export interface SiteMap {
    players:Path
    playerLogin:Path
  }

  const competitionEvents:Std.JsMap<boolean> = {
    createdCompetition: true,
    confirmedCompetition: true,
    playerAccepted: true,
    playerDeclined: true
  }
  
  const gameEvents:Std.JsMap<boolean> ={
    gameStarted: true,
    cardPlayed: true  
  }
  
  const playerEvents:Std.JsMap<boolean> ={
    playerLogOn:true, 
    playerLogOff:true 
  }
  
  export function isCompetitionEvent(str:string) {
    return competitionEvents[str] === true;
  }

  export function isGameEvent(str:string) {
    return gameEvents[str] === true;
  }
  
  export function isPlayerEvent(str:string) {
    return playerEvents[str] === true;
  }

}