import { Card, Path, Input, GameState, CompetitionState, Player, BriscolaEvent, CompetitionEvent, PlayerEvent } from "ddd-briscola-model"

export type Command = StarApplication | CreatePlayer | PlayerLogon | CompetitionCommand | PlayCard | SelectPlayerForCompetition |
  SetCompetitionKind | SetCompetitionDeadline | SetCurrentGame | DiplayPlayerDeck |
  UpdateGameState | UpdatePlayersState | UpdateCompetitionState | NewDomainEvent

export class StarApplication {
  public type: "startApplication" = "startApplication"
  constructor() { }
}

export class CreatePlayer {
  public type: "createPlayer" = "createPlayer"
  constructor(public playerName: string, public password: string) { }
}

export class PlayerLogon {
  public type: "playerLogon" = "playerLogon"
  constructor(public playerName: string, public password: string) { }
}

export class StartCompetition {
  public type: "startCompetition" = "startCompetition"
  constructor() { }
}

export class AcceptCompetition {
  public type: "acceptCompetition" = "acceptCompetition"
  constructor(public competition: Path) { }
}

export class DeclineCompetition {
  public type: "declineCompetition" = "declineCompetition"
  constructor(public competition: Path) { }
}

export type CompetitionCommand = StartCompetition | AcceptCompetition | DeclineCompetition

export class PlayCard {
  public type: "playCard" = "playCard"
  constructor(public card: Card) { }
}

export class SelectPlayerForCompetition {
  public type: "selectPlayerForCompetition" = "selectPlayerForCompetition"
  constructor(public player: Path, public selected: boolean) { }
}

export class SetCompetitionKind {
  public type: "setCompetitionKind" = "setCompetitionKind"
  constructor(public kind: Input.MatchKind) { }
}

export class SetCompetitionDeadline {
  public type: "setCompetitionDeadline" = "setCompetitionDeadline"
  constructor(public deadlineKind: Input.CompetitionStartDeadline) { }
}

export class SetCurrentGame {
  public type: "setCurrentGame" = "setCurrentGame"
  constructor(public game: Path) { }
}

export class DiplayPlayerDeck {
  public type: "diplayPlayerDeck" = "diplayPlayerDeck"
  constructor(public game: Path, public display: boolean) { }
}

export class UpdateGameState {
  public type: "updateGameState" = "updateGameState"
  constructor(public gameState: GameState) { }
}

export class UpdatePlayersState {
  public type: "updatePlayersState" = "updatePlayersState"
  constructor(public players: Player[]) { }
}

export class UpdateCompetitionState {
  public type: "updateCompetitionState" = "updateCompetitionState"
  constructor(public competitionState: CompetitionState) { }
}

export class NewDomainEvent {
  public type: "newDomainEvent" = "newDomainEvent"
  constructor(public event: (BriscolaEvent | CompetitionEvent | PlayerEvent)) { }
}