import {JsMap, Option} from "flib"
import {Path, DomainEvent, ByKindChoice, byKindChoice} from "./model"
import {Player} from "./player"
import {link, convert, Converter, ConstructorType, SimpleConverter, ChooseConverter, ByPropertyChooseConverter} from "rest-fetch"

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

export const toCompetitionStartDeadline = ChooseConverter.create((wso:{kind:string}) => {
  switch(wso.kind) {
    case CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.allPlayers] : return Option.some(AllPlayers)
    case CompetitionStartDeadlineKind[CompetitionStartDeadlineKind.onPlayerCount] :return Option.some(OnPlayerCount)
    default: throw new Error(`invalid CompetitionStartDeadline : ${JSON.stringify(wso)}`)
  }
})

const stringToCompetitionStartDeadlineKind = SimpleConverter.fromString.andThen(Converter.toEnum<CompetitionStartDeadlineKind>(CompetitionStartDeadlineKind, "CompetitionStartDeadlineKind"))

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


export const toMatchKind = ChooseConverter.create((wso:{kind:string}) => {
  switch(wso.kind) {
    case MatchKindKind[MatchKindKind.singleMatch] : return Option.some(SingleMatch)
    case MatchKindKind[MatchKindKind.numberOfGamesMatchKind] :return Option.some(NumberOfGamesMatchKind)
    case MatchKindKind[MatchKindKind.targetPointsMatchKind] :return Option.some(TargetPointsMatchKind)
    default: return Option.none<ConstructorType<any>>()
  }
}, "match kind" )

const stringToMatchKindKind = SimpleConverter.fromString.andThen(Converter.toEnum<MatchKindKind>(MatchKindKind, "MatchKindKind"))

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
  @link({arrayOf:Player})
  players: Player[]

  @convert(toMatchKind)
  kind: MatchKind

  @convert(toCompetitionStartDeadline)
  deadline: CompetitionStartDeadline
}

const stringToCompetitionStateKind = SimpleConverter.fromString.andThen(Converter.toEnum<CompetitionStateKind>(CompetitionStateKind, "CompetitionStateKind"))

export class CompetitionState {
  self: Path

  @convert(stringToCompetitionStateKind)
  kind: CompetitionStateKind

  @convert()
  competition: Competition

  @link({arrayOf:Player})
  acceptingPlayers: Player[]
  @link({arrayOf:Player})
  decliningPlayers: Player[]

  @convert(SimpleConverter.optional(SimpleConverter.fromString))
  accept: Option<Path>
  @convert(SimpleConverter.optional(SimpleConverter.fromString))
  decline: Option<Path>
}

export const competitionStateChoice = byKindChoice(() => [{
  key:CompetitionStateKind[CompetitionStateKind.dropped],
  value:CompetitionState
}, {
  key:CompetitionStateKind[CompetitionStateKind.fullfilled],
  value:CompetitionState
}, {
  key:CompetitionStateKind[CompetitionStateKind.open],
  value:CompetitionState
}], "competion state")