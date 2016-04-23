import {Card, Board, Path, Input, GameState, CompetitionState, Player, BriscolaEvent, CompetitionEvent, PlayerEvent} from "ddd-briscola-model"
import CommandID from "./CommandID"

export abstract class Command {
    type:CommandID
}

export class StarApplication extends Command {
  public type = CommandID.startApplication
  constructor() {
    super()
  }
}

export class CreatePlayer extends Command {
  public type = CommandID.createPlayer
  constructor(public playerName: string, public password:string) {
    super()
  }
}

export class PlayerLogon extends Command {
  public type = CommandID.playerLogon
  constructor(public playerName: string, public password:string) {
    super()
  }
}

export class StartCompetition extends Command {
  public type = CommandID.startCompetition
  constructor() {
    super()
  }
}

export class AcceptCompetition extends Command {
  public type = CommandID.acceptCompetition
  constructor(public competition: Path) {
    super()
  }
}

export class DeclineCompetition extends Command {
  public type = CommandID.declineCompetition
  constructor(public competition: Path) {
    super()
  }
}

export class PlayCard extends Command {
  public type = CommandID.playCard
  constructor(public card: Card) {
    super()
  }
}

export class SelectPlayerForCompetition extends Command {
  public type = CommandID.selectPlayerForCompetition
  constructor(public player: Path, public selected:boolean) {
    super()
  }
}

export class SetCompetitionKind extends Command {
  public type = CommandID.setCompetitionKind
  constructor(public kind: Input.MatchKind) {
    super()
  }
}

export class SetCompetitionDeadline extends Command {
  public type = CommandID.setCompetitionDeadline
  constructor(public deadlineKind: Input.CompetitionStartDeadline) {
    super()
  }
}

export class SetCurrentGame extends Command {
  public type = CommandID.setCurrentGame
  constructor(public game: Path) {
    super()
  }
}

export class DiplayPlayerDeck extends Command {
  public type = CommandID.diplayPlayerDeck
  constructor(public game:Path, public display:boolean) {
    super()
  }
}

export class UpdateGameState extends Command {
  public type = CommandID.updateGameState
  constructor(public gameState:GameState) {
    super()
  }
}

export class UpdatePlayersState extends Command {
  public type = CommandID.updatePlayersState
  constructor(public players:Player[]) {
    super()
  }
}

export class UpdateCompetitionState extends Command {
  public type = CommandID.updateCompetitionState
  constructor(public competitionState:CompetitionState) {
    super()
  }
}

export class NewDomainEvent extends Command {
  public type = CommandID.newDomainEvent
  constructor(public event:(BriscolaEvent | CompetitionEvent | PlayerEvent)) {
    super()
  }
}