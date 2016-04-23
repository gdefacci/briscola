import {Option} from "flib"
import * as Commands from "./Commands"
import {PlayersService} from "./PlayersService"
import {PlayerService} from "./PlayerService"
import {fetch} from "rest-fetch"
import {Board, SiteMap, GameState, ActiveGameState, FinalGameState, Player, CurrentPlayer, CompetitionState, ViewFlag} from "ddd-briscola-model"
import {copy} from "./Util"

export type Command = Commands.Command

export interface ApplicationState {
  playersService: PlayersService
  playerService: Option<PlayerService>
  board: Board
}

export function initialState(entryPoint: string): Promise<ApplicationState> {
  return fetch(SiteMap).from(entryPoint).then(siteMap => {
    return {
      playersService: new PlayersService(siteMap),
      playerService: Option.none<PlayerService>(),
      board: Board.empty()
    }
  })
}

export type ReducerType = (state: ApplicationState, command: Command, dispatch: (cmd: Command) => Promise<ApplicationState>) => ApplicationState
export type AsynchReducerType = (state: ApplicationState, command: Command, dispatch: (cmd: Command) => Promise<ApplicationState>) => Promise<ApplicationState>

export function synchReducer(rt: ReducerType): AsynchReducerType {
  return (state: ApplicationState, command: Command, dispatch: (cmd: Command) => Promise<ApplicationState>) => Promise.resolve(rt(state, command, dispatch))
}

export const playerLogon: AsynchReducerType = (state, command, dispatch) => {
  if (command instanceof Commands.PlayerLogon || command instanceof Commands.CreatePlayer) {
    const ps = state.playersService
    const createPlayer = (command instanceof Commands.PlayerLogon) ? ps.logon(command.playerName, command.password) : ps.createPlayer(command.playerName, command.password)
    return createPlayer.then(player => {
      const playerService = new PlayerService(player)
      playerService.eventsLog.subscribe(event => {
        dispatch(new Commands.NewDomainEvent(event))
      })

      playerService.gamesChannel.subscribe(es => {
        dispatch(new Commands.UpdateGameState(es.game))
      })

      playerService.competitionsChannel.subscribe(es => {
        dispatch(new Commands.UpdateCompetitionState(es.competition))
      })

      playerService.playersChannel.subscribe(es => {
        dispatch(new Commands.UpdatePlayersState(es.players))
      })
      const board = copy(state.board, {
        player:Option.some(player)
      })
      return {
        playersService: state.playersService,
        playerService: Option.some(playerService),
        board
      }
    })
  } else return Promise.resolve(state)
}

export const playCard: AsynchReducerType = (state, command) => {
  if (command instanceof Commands.PlayCard) {
    return state.playerService.map(playerService => {
      const game = state.board.currentGame.map(g => Promise.resolve(g.self)).getOrElse(() => Promise.reject("no current game"))
      return game.then(game => {
        playerService.playCard(game, command.card)
        return {
          playersService: state.playersService,
          playerService: Option.some(playerService),
          board: state.board
        }
      })
    }).getOrElse(() => Promise.reject("player service not avaiable"))
  } else {
    return Promise.resolve(state)
  }
}

function playerServiceEffect(effect: (ps: PlayerService, state: ApplicationState, command: Command) => any): AsynchReducerType {
  return (state, command) => {
    return state.playerService.map(playerService => {
      effect(playerService, state, command)
      return Promise.resolve(state)
    }).getOrElse(() => Promise.reject("player service not avaiable"))
  }
}

export const competitionCommands: AsynchReducerType = (state, command, dispacth) => {
  let res: AsynchReducerType
  if (command instanceof Commands.StartCompetition) {
    res = playerServiceEffect(playerService =>
      playerService.createCompetition(Object.keys(state.board.competitionSelectedPlayers), state.board.competitionKind, state.board.competitionDeadlineKind)
    )
  } else if (command instanceof Commands.AcceptCompetition) {
    res = playerServiceEffect(playerService => playerService.acceptCompetition(command.competition))
  } else if (command instanceof Commands.DeclineCompetition) {
    res = playerServiceEffect(playerService => playerService.declineCompetition(command.competition))
  } else {
    res = () => Promise.reject(`invalid command ${command.type} `)
  }
  return res(state, command, dispacth)
}

function boardReducer(br: (board: Board, command: Command) => Board): ReducerType {
  return (state, command) => {
    return {
      playersService: state.playersService,
      playerService: state.playerService,
      board: br(state.board, command)
    }
  }
}

export const selectPlayerForCompetition: ReducerType = boardReducer((board, command) => {
  if (command instanceof Commands.SelectPlayerForCompetition) {
    if (command.selected) {
      return copy(board, {
        competitionSelectedPlayers: {
          [command.player]: true
        }
      })
    } else {
      const cpy = copy(board, {})
      delete cpy.competitionSelectedPlayers[command.player];
      return cpy
    }
  } else {
    return board
  }
})

export const setCompetitionKind: ReducerType = boardReducer((board, command) => {
  if (command instanceof Commands.SetCompetitionKind) {
    return copy(board, {
      competitionKind: command.kind
    })
  } else {
    return board
  }
})

export const setCompetitionDeadline: ReducerType = boardReducer((board, command) => {
  if (command instanceof Commands.SetCompetitionDeadline) {
    return copy(board, {
      competitionDeadlineKind: command.deadlineKind
    })
  } else {
    return board
  }
})

export const setCurrentGame: ReducerType = boardReducer((board, command) => {
  if (command instanceof Commands.SetCurrentGame) {
    return copy(board, {
      currentGame: Option.option<GameState>(board.activeGames[command.game]).orElse(() => Option.option(board.finishedGames[command.game]))
    })
  } else {
    return board;
  }
})

export const diplayPlayerDeck: ReducerType = boardReducer((board, command) => {
  if (command instanceof Commands.DiplayPlayerDeck) {
    return copy(board, {
      viewFlag: command.display === true ? ViewFlag.showPlayerCards : ViewFlag.normal
    })
  } else {
    return board;
  }
})

export const updatePlayersState: ReducerType = boardReducer((board, command) => {
  if (command instanceof Commands.UpdatePlayersState) {
    return copy(board, {
      players: board.player.map(cp => command.players.filter(pl => pl.self !== cp.self)).getOrElse(() => command.players)
    })
  } else {
    return board;
  }
})

export const updateGameState: ReducerType = boardReducer((board, command) => {
  if (command instanceof Commands.UpdateGameState) {
    const gm = command.gameState
    let res: Board = board
    const currentGame = board.currentGame.fold<void>(
      () => Option.some(gm),
      (cgm) => {
        if (gm.self === cgm.self) {
          return Option.some(gm)
        } else {
          return Option.some(cgm)
        }
      })
    if (gm instanceof ActiveGameState) {
      res = copy(board, {
        currentGame,
        activeGames: {
          [gm.self]: gm
        }
      });
    } else if (gm instanceof FinalGameState) {
      res = copy(board, {
        currentGame,
        finishedGames: {
          [gm.self]: gm
        }
      })
      delete res.activeGames[gm.self];
    }
    return res;
  } else {
    return board;
  }
})

export const updateCompetionState: ReducerType = boardReducer((board, command) => {
  if (command instanceof Commands.UpdateCompetitionState) {
    return copy(board, {
      engagedCompetitions: {
        [command.competitionState.self]: command.competitionState
      }
    })
  } else {
    return board;
  }
})

export const newDomainEvent: ReducerType = boardReducer((board, command) => {
  if (command instanceof Commands.NewDomainEvent) {
    return copy(board, {
      eventsLog: [command.event].concat(board.eventsLog)
    })
  } else {
    return board;
  }
})