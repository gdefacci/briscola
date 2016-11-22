import { Option } from "flib"
import { CompetitionState } from "./competition"
import { DomainEvent } from "./model"
import { Player } from "./player"
import { link, convert, Value, ChoiceValue, JsConstructor, Lazy } from "nrest-fetch"

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
  @convert(Value.option(Value.string))
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

export const competitionEvents: [(a: any) => boolean, () => JsConstructor<any>][] = [
  [
    a => a.kind === CompetitionEventKind[CompetitionEventKind.createdCompetition],
    () => CreatedCompetition
  ], [
    a => a.kind === CompetitionEventKind[CompetitionEventKind.confirmedCompetition],
    () => ConfirmedCompetition
  ], [
    a => a.kind === CompetitionEventKind[CompetitionEventKind.playerAccepted],
    () => CompetitionAccepted
  ], [
    a => a.kind === CompetitionEventKind[CompetitionEventKind.playerDeclined],
    () => CompetitionDeclined
  ]
]

export const competitionEventChoice:() => ChoiceValue<any> = Lazy.choose("CompetitionEvent", ...competitionEvents)
