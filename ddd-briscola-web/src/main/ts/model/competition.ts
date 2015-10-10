module Model {

  import Option = Std.Option

  export enum CompetitionEventKind {
    createdCompetition, confirmedCompetition, playerAccepted, playerDeclined
  }

  export enum CompetitionStateKind {
    empty, open, dropped, fullfilled
  }

  export enum CompetitionStartDeadlineKind {
    onPlayerCount
  }

  export type CompetitionEvent = CompetitionDeclined | CompetitionAccepted | ConfirmedCompetition | CreatedCompetition

  export class CompetitionDeclined extends DomainEvent {
    constructor(public player: Player,
      public competition: CompetitionState,
      public reason: Option<string>) {
      super(CompetitionEventKind[CompetitionEventKind.playerDeclined])
    }
  }

  export class CompetitionAccepted extends DomainEvent {
    constructor(public player: Player,
      public competition: CompetitionState) {
      super(CompetitionEventKind[CompetitionEventKind.playerAccepted])
    }
  }

  export class ConfirmedCompetition extends DomainEvent {
    constructor(public competition: CompetitionState) {
      super(CompetitionEventKind[CompetitionEventKind.confirmedCompetition])
    }
  }

  export class CreatedCompetition extends DomainEvent {
    constructor(public issuer: Player,
      public competition: CompetitionState) {
      super(CompetitionEventKind[CompetitionEventKind.createdCompetition])
    }
  }

  export class CompetitionState extends Aggregate {
    constructor(public self: Ws.Path,
      public kind: CompetitionStateKind,
      public competition: Competition,
      public acceptingPlayers: Player[],
      public decliningPlayers: Player[],
      public accept: Option<Ws.Path>,
      public decline: Option<Ws.Path>) {
      super()
    }
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

    export type MatchKind = string | NumberOfGamesMatchKind | TargetPointsMatchKind

  export interface NumberOfGamesMatchKind {
    numberOfMatches: number
    kind: MatchKindKind
  }

  export interface TargetPointsMatchKind {
    winnerPoints: number
    kind: MatchKindKind
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

}