import {Option} from "flib"
import {Path} from "./model"
import {Player} from "./player"
import {arrayOfLinks, convert, Value, ChoiceValue, Lazy} from "nrest-fetch"
import {toEnum} from "./Util"

export enum MatchKindKind {
  singleMatch, numberOfGamesMatchKind, targetPointsMatchKind
}

export enum CompetitionStateKind {
  open, dropped, fullfilled
}

export enum CompetitionStartDeadlineKind {
  allPlayers, onPlayerCount
}

export type CompetitionStartDeadline = AllPlayers | OnPlayerCount

export const toCompetitionStartDeadline:() => ChoiceValue<any> = Lazy.choose("CompetitionStartDeadline", [
  a => a.kind === CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.allPlayers],
  () => AllPlayers
], [
  a => a.kind === CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.onPlayerCount],
  () => OnPlayerCount
])

export class AllPlayers {
  get kind(): CompetitionStartDeadlineKind {
    return CompetitionStartDeadlineKind.allPlayers
  }
}

export class OnPlayerCount {
  count: number
  get kind(): CompetitionStartDeadlineKind {
    return CompetitionStartDeadlineKind.onPlayerCount
  }
}

export type MatchKind = SingleMatch | NumberOfGamesMatchKind | TargetPointsMatchKind

/*
export function toMatchKind(kind:string):MatchKind {
  switch(kind) {
    case MatchKindKind[MatchKindKind.singleMatch] : return new SingleMatch
    case MatchKindKind[MatchKindKind.numberOfGamesMatchKind] :return new NumberOfGamesMatchKind
    case MatchKindKind[MatchKindKind.targetPointsMatchKind] :return new TargetPointsMatchKind
    default: throw new Error("invalid match kind "+kind)
  }
}
*/
export const matchKindChoice = Lazy.choose("MatchKind", [
  (a) => a.kind === MatchKindKind[MatchKindKind.singleMatch],
  () => SingleMatch
], [
  (a) => a.kind === MatchKindKind[MatchKindKind.targetPointsMatchKind],
  () => TargetPointsMatchKind
])

export class SingleMatch {
  get kind(): MatchKindKind {
    return MatchKindKind.singleMatch
  }
}

export class NumberOfGamesMatchKind {
  numberOfMatches: number
  get kind(): MatchKindKind {
    return MatchKindKind.numberOfGamesMatchKind
  }
}

export class TargetPointsMatchKind {
  winnerPoints: number
  get kind(): MatchKindKind {
    return MatchKindKind.numberOfGamesMatchKind
  }
}

export class Competition {
  @convert( arrayOfLinks(Player) )
  players: Player[]

  @convert(matchKindChoice)
  kind: MatchKind

  @convert(toCompetitionStartDeadline)
  deadline: CompetitionStartDeadline
}

const stringToCompetitionStateKind = Value.string().map(toEnum(CompetitionStateKind, "CompetitionStateKind"))

export class CompetitionState {
  self: Path

  @convert(() => stringToCompetitionStateKind)
  kind: CompetitionStateKind

  @convert()
  competition: Competition

  @convert(arrayOfLinks(Player))
  acceptingPlayers: Player[]
  @convert(arrayOfLinks(Player))
  decliningPlayers: Player[]

  @convert(Value.option(Value.string))
  accept: Option<Path>
  @convert(Value.option(Value.string))
  decline: Option<Path>
}

export const competitionStateChoice = Lazy.choose("CompetitionState", [
  a => a.kind ===CompetitionStateKind[CompetitionStateKind.dropped],
  () => CompetitionState
], [
  a => a.kind ===CompetitionStateKind[CompetitionStateKind.fullfilled],
  () => CompetitionState
], [
  a => a.kind ===CompetitionStateKind[CompetitionStateKind.open],
  () => CompetitionState
])