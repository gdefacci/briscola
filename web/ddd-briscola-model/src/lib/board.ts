import {JsMap, Option} from "flib"
import {GameState, ActiveGameState, FinalGameState} from "./game"
import {BriscolaEvent} from "./gameEvents"
import {CompetitionState} from "./competition"
import {CompetitionEvent} from "./competitionEvents"
import {Player, PlayerEvent} from "./player"
import * as Input from "./input"

export module Board {
  export function empty():Board {
    return {
      player:Option.None,
      players:[],
      currentGame:Option.None,
      activeGames:{},
      finishedGames:{},
      engagedCompetitions:{},
      competitionSelectedPlayers:{},
      competitionKind:"single-match",
      competitionDeadlineKind: "all-players",
      eventsLog:[],
      viewFlag:ViewFlag.normal,
      config:{
        minPlayersNumber:2,
        maxPlayersNumber:8
      }
    }
  }
}

/**
  * FIXME fetch from server, put inside SiteMap?
  */
export interface AppConfig {
  minPlayersNumber:number
  maxPlayersNumber:number
}

export enum ViewFlag {
  normal, showPlayerCards
}

export interface Board {
  config:AppConfig

  player:Option<Player>
  players:Player[]

  currentGame:Option<GameState>
  activeGames:JsMap<ActiveGameState>
  finishedGames:JsMap<FinalGameState>

  engagedCompetitions:JsMap<CompetitionState>
  competitionSelectedPlayers:JsMap<boolean>
  competitionKind:Input.MatchKind
  competitionDeadlineKind: Input.CompetitionStartDeadline

  eventsLog:(BriscolaEvent | CompetitionEvent | PlayerEvent)[]

  viewFlag:ViewFlag
}



