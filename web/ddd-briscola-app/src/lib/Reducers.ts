import { Option } from "flib"
import * as Commands from "./Command"
import { PlayerService } from "./PlayerService"
import { Board, GameState, ActiveGameState, FinalGameState, ViewFlag } from "ddd-briscola-model"
import { copy } from "./Util"
import {ApplicationState} from "./ApplicationState"

export type Command = Commands.Command

export type StateChange = (state: ApplicationState, dispatch: (cmd: Command) => Promise<ApplicationState>) => ApplicationState
export type AsynchStateChange = (state: ApplicationState, dispatch: (cmd: Command) => Promise<ApplicationState>) => Promise<ApplicationState>

export type ReducerType<C> = (command: C) => StateChange
export type AsynchReducerType<C> = (command: C) => AsynchStateChange

export function synchReducer<C>(rt: ReducerType<C>): AsynchReducerType<C> {
  return (command: C) => synchStateChange(rt(command))
}
export function synchStateChange(sc:StateChange):AsynchStateChange {
  return (st, d) => {
    try {
      const r = sc(st,d)
      return Promise.resolve(r)
    } catch(e) {
      return Promise.reject(e)
    }
  }
}

export const playerLogon: AsynchReducerType<Commands.PlayerLogon | Commands.CreatePlayer> = (command) => (state, dispatch): Promise<ApplicationState> => {
  const ps = state.playersService
  const createPlayer = (command instanceof Commands.PlayerLogon) ? ps.logon(command.playerName, command.password) : ps.createPlayer(command.playerName, command.password)
  return createPlayer.then(player => {
    const playerService = state.createPlayerService(player)
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
      player: Option.some(player)
    })
    const {createPlayerService, playersService} = state
    return {
      playersService,
      playerService: Option.some(playerService),
      board,
      createPlayerService
    }
  })
}

export const playCard: AsynchReducerType<Commands.PlayCard> = (command) => (state) => {
  return state.playerService.map(playerService => {
    const game: Promise<string> = state.board.currentGame.map(g => Promise.resolve(g.self)).getOrElse(() => Promise.reject("no current game"))
    return game.then(game => {
      playerService.playCard(game, command.card);

      const {createPlayerService, playersService, board} = state
      const nst: ApplicationState = {
        playersService,
        playerService: Option.some(playerService),
        board,
        createPlayerService,
      };
      return nst;
    })
  }).getOrElse(() => Promise.reject("player service not avaiable"))
}

function playerServiceEffect<C>(effect: (ps: PlayerService, state: ApplicationState, command: C) => any): AsynchReducerType<C> {
  return (command) => (state) => {
    return state.playerService.map(playerService => {
      effect(playerService, state, command)
      return Promise.resolve(state)
    }).getOrElse(() => Promise.reject("player service not avaiable"))
  }
}

export const competitionCommands: AsynchReducerType<Commands.CompetitionCommand> = (command) => (state, dispacth) => {
  let res: AsynchReducerType<Commands.CompetitionCommand>
  if (command instanceof Commands.StartCompetition) {
    res = playerServiceEffect<Commands.StartCompetition>(playerService =>
      playerService.createCompetition(Object.keys(state.board.competitionSelectedPlayers), state.board.competitionKind, state.board.competitionDeadlineKind)
    )
  } else if (command instanceof Commands.AcceptCompetition) {
    res = playerServiceEffect<Commands.AcceptCompetition>(playerService => playerService.acceptCompetition(command.competition))
  } else { // if (command instanceof Commands.DeclineCompetition) {
    res = playerServiceEffect<Commands.DeclineCompetition>(playerService => playerService.declineCompetition(command.competition))
  }
  return res(command)(state, dispacth)
}

function boardReducer<C>(br: (command: C) => (board: Board) => Board): ReducerType<C> {
  return (command) => (state) => {
    const {createPlayerService, playersService, playerService} = state
    return {
      playersService,
      playerService,
      board: br(command)(state.board),
      createPlayerService
    }
  }
}

export const selectPlayerForCompetition: ReducerType<Commands.SelectPlayerForCompetition> = boardReducer<Commands.SelectPlayerForCompetition>((command) => (board) => {
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
})

export const setCompetitionKind = boardReducer<Commands.SetCompetitionKind>((command) => (board) => {
  return copy(board, {
    competitionKind: command.kind
  })
})

export const setCompetitionDeadline = boardReducer<Commands.SetCompetitionDeadline>((command) => (board) => {
  return copy(board, {
    competitionDeadlineKind: command.deadlineKind
  })
})

export const setCurrentGame = boardReducer<Commands.SetCurrentGame>((command) => (board) => {
  return copy(board, {
    currentGame: Option.option<GameState>(board.activeGames[command.game]).orElse(() => Option.option(board.finishedGames[command.game]))
  })
})

export const diplayPlayerDeck = boardReducer<Commands.DiplayPlayerDeck>((command) => (board) => {
  return copy(board, {
    viewFlag: command.display === true ? ViewFlag.showPlayerCards : ViewFlag.normal
  })
})

export const updatePlayersState = boardReducer<Commands.UpdatePlayersState>((command) => (board) => {
  return copy(board, {
    players: board.player.map(cp => command.players.filter(pl => pl.self !== cp.self)).getOrElse(() => command.players)
  })
})

export const updateGameState = boardReducer<Commands.UpdateGameState>((command) => (board) => {
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
})

export const updateCompetionState = boardReducer<Commands.UpdateCompetitionState>((command) => (board) => {
  return copy(board, {
    engagedCompetitions: {
      [command.competitionState.self]: command.competitionState
    }
  })
})

export const newDomainEvent = boardReducer<Commands.NewDomainEvent>((command) => (board) => {
  return copy(board, {
    eventsLog: [command.event].concat(board.eventsLog)
  })
})