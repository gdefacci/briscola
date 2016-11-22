import { Card, Path, Input, GameState, CompetitionState, Player, BriscolaEvent, CompetitionEvent, PlayerEvent } from "ddd-briscola-model"

export type Command = StarApplication | CreatePlayer | PlayerLogon | CompetitionCommand | PlayCard | SelectPlayerForCompetition |
                      SetCompetitionKind | SetCompetitionDeadline | SetCurrentGame | DiplayPlayerDeck |
                      UpdateGameState | UpdatePlayersState | UpdateCompetitionState | NewDomainEvent

export class StarApplication {
  type: "startApplication"
  constructor() {}
}

export class CreatePlayer {
  type: "createPlayer"
  constructor(public playerName: string, public password: string) {}
}

export class PlayerLogon {
  type: "playerLogon"
  constructor(public playerName: string, public password: string) {}
}

export class StartCompetition {
  type: "startCompetition"
  constructor() {}
}

export class AcceptCompetition {
  type: "acceptCompetition"
  constructor(public competition: Path) {}
}

export class DeclineCompetition {
  type: "declineCompetition"
  constructor(public competition: Path) {}
}

export type CompetitionCommand = StartCompetition | AcceptCompetition | DeclineCompetition

export class PlayCard {
  type: "playCard"
  constructor(public card: Card) {}
}

export class SelectPlayerForCompetition {
  type: "selectPlayerForCompetition"
  constructor(public player: Path, public selected: boolean) {}
}

export class SetCompetitionKind {
  type: "setCompetitionKind"
  constructor(public kind: Input.MatchKind) {}
}

export class SetCompetitionDeadline {
  type: "setCompetitionDeadline"
  constructor(public deadlineKind: Input.CompetitionStartDeadline) {}
}

export class SetCurrentGame {
  type: "setCurrentGame"
  constructor(public game: Path) {}
}

export class DiplayPlayerDeck {
  type: "diplayPlayerDeck"
  constructor(public game: Path, public display: boolean) {}
}

export class UpdateGameState {
  type: "updateGameState"
  constructor(public gameState: GameState) {}
}

export class UpdatePlayersState {
  type: "updatePlayersState"
  constructor(public players: Player[]) {}
}

export class UpdateCompetitionState {
  type: "updateCompetitionState"
  constructor(public competitionState: CompetitionState) {}
}

export class NewDomainEvent {
  type: "newDomainEvent"
  constructor(public event: (BriscolaEvent | CompetitionEvent | PlayerEvent)) {}
}