import {Path} from "./model"
import {Seed, Card as ModelCard} from "./game"

export type MatchKind = string | NumberOfGamesMatchKind | TargetPointsMatchKind

export interface NumberOfGamesMatchKind {
  numberOfMatches:number
}

export interface TargetPointsMatchKind {
  winnerPoints:number
}

export type CompetitionStartDeadline = string | OnPlayerCountDeadline

export interface OnPlayerCountDeadline {
  count:number
  kind:string
}

export interface Player {
  name:string
  password:string  
}

export interface Competition {
  players:Path[]
  kind:MatchKind,
  deadline:CompetitionStartDeadline
}

export interface Card {
  seed:string
  "number":number  
}

export function card(m:ModelCard):Card {
    return {
      number: m.number,
      seed: Seed[m.seed]
    }  
  }

