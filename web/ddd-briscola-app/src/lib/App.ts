import {Option} from "flib"
import * as Commands from "./Commands"
import {PlayersService} from "./PlayersService"
import {PlayerService} from "./PlayerService"
import CommandID from "./CommandID"
import {Board, SiteMap, GameState, ActiveGameState, FinalGameState, Player, CurrentPlayer, CompetitionState, ViewFlag} from "ddd-briscola-model"
import {fetch} from "rest-fetch"
import * as Reducers from "./Reducers"
import CommandDispatcher from "./CommandsDispatcher"

import Command = Commands.Command

export interface App {
  displayChannel: Rx.Observable<Board>
  exec(cmd: Command):void
}

export module App {
  export function create(entryPoint:string):App {
    return new AppImpl(entryPoint)
  }
}

class AppImpl implements App {

  static dispatch(cmd:Reducers.Command):Reducers.AsynchReducerType {
    switch (cmd.type) {
      case CommandID.startApplication : return Reducers.synchReducer( st => st )
      case CommandID.createPlayer:
      case CommandID.playerLogon: return Reducers.playerLogon
      case CommandID.playCard: return Reducers.playCard
      case CommandID.acceptCompetition:
      case CommandID.declineCompetition:
      case CommandID.startCompetition: return Reducers.competitionCommands
      case CommandID.diplayPlayerDeck: return Reducers.synchReducer( Reducers.diplayPlayerDeck )
      case CommandID.newDomainEvent: return Reducers.synchReducer( Reducers.newDomainEvent )
      case CommandID.selectPlayerForCompetition: return Reducers.synchReducer( Reducers.selectPlayerForCompetition )
      case CommandID.setCompetitionDeadline: return Reducers.synchReducer( Reducers.setCompetitionDeadline )
      case CommandID.setCompetitionKind: return Reducers.synchReducer( Reducers.setCompetitionKind )
      case CommandID.setCurrentGame: return Reducers.synchReducer( Reducers.setCurrentGame )
      case CommandID.updateCompetitionState: return Reducers.synchReducer( Reducers.updateCompetionState )
      case CommandID.updateGameState: return Reducers.synchReducer( Reducers.updateGameState )
      case CommandID.updatePlayersState: return Reducers.synchReducer( Reducers.updatePlayersState )
      default:
        throw new Error(`invalid command ${cmd}`);
    }
  }

  displayChannel: Rx.Observable<Board>
  dispatcher:Promise<CommandDispatcher<Command, Reducers.ApplicationState>>
  constructor(entryPoint: string) {
    const initialState = Reducers.initialState(entryPoint)
    this.dispatcher = initialState.then( state =>
      new CommandDispatcher(AppImpl.dispatch, state)
    )
    this.displayChannel =  Rx.Observable.fromPromise(this.dispatcher).flatMap( d => d.changes().map( s => s.board ) );
    this.displayChannel.subscribe(ev => {
      console.log("display channel ")
      console.log(ev)
      console.log("***************")
    }, err => {
      console.error(err)
    })
  }

  exec(cmd: Command):void {
    this.dispatcher.then( d => {
      d.dispatch(cmd).catch( err => console.error(err) )
    });
  }
}