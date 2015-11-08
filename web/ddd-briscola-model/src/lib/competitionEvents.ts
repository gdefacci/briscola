import {Option} from "flib"
import {CompetitionState} from "./competition"
import {Path, DomainEvent, ByKindChoice, byKindChoice} from "./model"
import {Player} from "./player"
import {link, convert, Converter, ConstructorType, ChooseConverter, ByPropertyChooseConverter} from "rest-fetch"

export enum CompetitionEventKind {
  createdCompetition, confirmedCompetition, playerAccepted, playerDeclined
}

export type CompetitionEvent = CompetitionDeclined | CompetitionAccepted | ConfirmedCompetition | CreatedCompetition

export class CompetitionDeclined implements DomainEvent {
  get eventName() {
     return CompetitionEventKind[CompetitionEventKind.playerDeclined]
  }

  @link()
  player: Player
  @link()
  competition: CompetitionState
  reason: Option<string>
}

export class CompetitionAccepted implements DomainEvent {
  get eventName() {
     return CompetitionEventKind[CompetitionEventKind.playerAccepted]
  }
  @link()
  player: Player
  @link()
  competition: CompetitionState
}

export class ConfirmedCompetition implements DomainEvent {
  get eventName() {
     return CompetitionEventKind[CompetitionEventKind.confirmedCompetition]
  }
  @link()
  competition: CompetitionState
}

export class CreatedCompetition implements DomainEvent {
  get eventName() {
     return CompetitionEventKind[CompetitionEventKind.createdCompetition]
  }
  @link()
  issuer: Player
  @link()
  competition: CompetitionState
}

export const competitionEventChoice = byKindChoice(() => [{
    key:CompetitionEventKind[CompetitionEventKind.createdCompetition],
    value:CreatedCompetition
  }, {
    key:CompetitionEventKind[CompetitionEventKind.confirmedCompetition],
    value:ConfirmedCompetition
  }, {
    key:CompetitionEventKind[CompetitionEventKind.playerAccepted],
    value:CompetitionAccepted
  }, {
    key:CompetitionEventKind[CompetitionEventKind.playerDeclined],
    value:CompetitionDeclined
  }], "competition event")
