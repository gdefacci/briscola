import { Option, JsMap } from "flib"
import * as Commands from "./Command"
import { Board, GameState, ActiveGameState, FinalGameState, ViewFlag } from "ddd-briscola-model"
import { copy } from "./Util"
import { ApplicationState } from "./ApplicationState"

export type Command = Commands.Command

export type StateChange = (state: ApplicationState, dispatch: (cmd: Command) => Promise<ApplicationState>) => ApplicationState
export type AsynchStateChange = (state: ApplicationState, dispatch: (cmd: Command) => Promise<ApplicationState>) => Promise<ApplicationState>

export type ReducerType<C> = (command: C) => StateChange
export type AsynchReducerType<C> = (command: C) => AsynchStateChange

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
    const game: Promise<ActiveGameState> = state.board.currentGame.fold(
      () => Promise.reject(new Error("no current game")),
      g => (g instanceof ActiveGameState) ? Promise.resolve(g) : Promise.reject(new Error("game is not active")))
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

export const competitionCommands: AsynchReducerType<Commands.CompetitionCommand> = (command) => (state, dispacth) => {
  return state.playerService.map(playerService => {
    if (command instanceof Commands.StartCompetition) {
      const resp = playerService.createCompetition(Object.keys(state.board.competitionSelectedPlayers), state.board.competitionKind, state.board.competitionDeadlineKind)
      return resp.then(r => state)
    } else {
      const competitionState = state.board.engagedCompetitions[command.competition]
      if (competitionState != null && competitionState != undefined) {
        if (command instanceof Commands.AcceptCompetition) {
          return playerService.acceptCompetition(competitionState).fold(
            () => Promise.reject("could not accept the competition"),
            () => Promise.resolve(state)
          )
        } else {
          return playerService.declineCompetition(competitionState).fold(
            () => Promise.reject("could not reject the competition"),
            () => Promise.resolve(state)
          )
        }
      } else return Promise.reject(new Error("Invalid competition"))
    }
  }).getOrElse(() => Promise.reject(new Error("Player service is unavaiable")))
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
      competitionSelectedPlayers:JsMap.merge([board.competitionSelectedPlayers, {
        [command.player]: true
      }])
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
      activeGames: JsMap.merge([board.activeGames, {
        [gm.self]: gm
      }])
    });
  } else if (gm instanceof FinalGameState) {
    res = copy(board, {
      currentGame,
      finishedGames: JsMap.merge([board.finishedGames, {
        [gm.self]: gm
      }])
    })
    delete res.activeGames[gm.self];
  }
  return res;
})

export const updateCompetionState = boardReducer<Commands.UpdateCompetitionState>((command) => (board) => {
  return copy(board, {
    engagedCompetitions: JsMap.merge([ board.engagedCompetitions, {
      [command.competitionState.self]: command.competitionState
    }])
  })
})

export const newDomainEvent = boardReducer<Commands.NewDomainEvent>((command) => (board) => {
  return copy(board, {
    eventsLog: [command.event].concat(board.eventsLog)
  })
})
