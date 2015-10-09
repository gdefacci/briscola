module Model.Input {

  export type MatchKind = string | NumberOfGamesMatchKind | TargetPointsMatchKind
  
  export interface NumberOfGamesMatchKind {
    numberOfMatches:number
  }
  
  export interface TargetPointsMatchKind {
    winnerPoints:number
  }
  
  export type CompetitionStartDeadline = String | OnPlayerCountDeadline

  export interface OnPlayerCountDeadline {
    count:number
    kind:string
  }
  
  export interface Player {
    name:string
    password:string  
  }
  
  export interface Competition {
    players:Model.Ws.Path[]
    kind:MatchKind,
    deadline:CompetitionStartDeadline
  }

  export interface Card {
    seed:string
    "number":number  
  }

}