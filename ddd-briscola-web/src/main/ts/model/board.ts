module Model {
  
  import JsMap = Std.JsMap
  import Option = Std.Option
  import ActiveGame = Model.ActiveGameState
  import FinalGameState = Model.FinalGameState
  import CompetitionState = Model.CompetitionState
  import BriscolaEvent = Model.BriscolaEvent
  import CompetitionEvent = Model.CompetitionEvent
  
  export module Board {
    export function empty():Board {
      return {
        player:Std.none<Model.Player>(),
        players:[],
        currentGame:Std.none<ActiveGameState>(),
        activaGames:{},
        finishedGames:{},
        engagedCompetitions:{},
        competitionSelectedPlayers:{},
        competitionKind:"single-match",
        competitionDeadlineKind: "all-players",
        eventsLog:[],
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
  interface AppConfig {
    minPlayersNumber:number
    maxPlayersNumber:number
  } 
  
  export interface Board {
    config:AppConfig  

    player:Option<Model.Player>
    players:Model.Player[]
    
    currentGame:Option<GameState>
    activaGames:JsMap<ActiveGameState>
    finishedGames:JsMap<FinalGameState>
    
    engagedCompetitions:JsMap<CompetitionState>
    competitionSelectedPlayers:JsMap<boolean>
    competitionKind:Model.Input.MatchKind
    competitionDeadlineKind: Model.Input.CompetitionStartDeadline
    
    eventsLog:(BriscolaEvent | CompetitionEvent | PlayerEvent)[]
    
    
  }



}