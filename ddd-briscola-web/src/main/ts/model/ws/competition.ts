namespace Model.Ws {

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

  export interface CompetitionState extends State {
    self?: Path
    kind: string
    competition?: Competition
    acceptingPlayers: Path[]
    decliningPlayers: Path[]
    accept?: Path
    decline?: Path
  }

  export type CompetitionStartDeadline = AllPlayers | OnPlayerCount

  export interface AllPlayers {
    kind: string
  }
  
  export interface OnPlayerCount {
    count: number
    kind: string
  }

  export interface Competition {
    players: Path[]
    kind: MatchKind
    deadline: CompetitionStartDeadline
  }

  export type MatchKind = SingleMatch | NumberOfGamesMatchKind | TargetPointsMatchKind

  export interface SingleMatch {
    kind: string
  }
  
  export interface NumberOfGamesMatchKind {
    numberOfMatches: number
    kind: string
  }

  export interface TargetPointsMatchKind {
    winnerPoints: number
    kind: string
  }

  const competitionEvents: Std.JsMap<boolean> = {
    createdCompetition: true,
    confirmedCompetition: true,
    playerAccepted: true,
    playerDeclined: true
  }

  export function isCompetitionEvent(str: string) {
    return competitionEvents[str] === true;
  }

}