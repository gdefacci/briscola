import {Card, Board, Path, Input} from "ddd-briscola-model"

export abstract class Command {
}

export abstract class GameCommand extends Command {
  constructor() {
    super()
  }
}

export abstract class CompetitionCommand extends Command {
  constructor() {
    super()
  }
}
/**
  * A command, after its execution the view need to be re-rendered (alias: its execution trigger an DisplayBoardCommand)
  */
export abstract class DisplayCommand extends Command {
  constructor() {
    super()
  }
}

export class DisplayBoardCommand extends Command {
  constructor(public board: Board) {
    super()
  }
}

export class CreatePlayer extends Command {
  constructor(public playerName: string, public password:string) {
    super()
  }
}

export class PlayerLogon extends Command {
  constructor(public playerName: string, public password:string) {
    super()
  }
}

export class StartCompetition extends CompetitionCommand {
  constructor() {
    super()
  }
}

export class AcceptCompetition extends CompetitionCommand {
  constructor(public competition: Path) {
    super()
  }
}

export class DeclineCompetition extends CompetitionCommand {
  constructor(public competition: Path) {
    super()
  }
}

export class PlayCard extends GameCommand {
  constructor(public card: Card) {
    super()
  }
}

export class SelectPlayerForCompetition extends DisplayCommand {
  constructor(public player: Path, public selected:boolean) {
    super()
  }
}

export class SetCompetitionKind extends DisplayCommand {
  constructor(public kind: Input.MatchKind) {
    super()
  }
}

export class SetCompetitionDeadline extends DisplayCommand {
  constructor(public deadlineKind: Input.CompetitionStartDeadline) {
    super()
  }
}

export class SetCurrentGame extends DisplayCommand {
  constructor(public game: Path) {
    super()
  }
}

export class DiplayPlayerDeck extends DisplayCommand {
  constructor(public game:Path, public display:boolean) {
    super()
  }
}

/*
export class StartCompetition extends PlayerCommand {
  constructor() {
    super()
  }
} 
*/


/**
  * CreatePlayer
  * StartCompetition
  * AcceptCompetition
  * DeclineCompetition
  * PlayCard
  * SetCurrentGame
  */

