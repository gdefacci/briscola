import { Command } from "./Command"
import { Board } from "ddd-briscola-model"
import commandDispatcher from "./CommandsDispatcher"
import { ApplicationState, initialState } from "./ApplicationState"
import applicationDispatch  from "./ApplicationDispatch"
import {AsynchStateChange} from "./Red"
import {Observable, ReplaySubject} from '@reactivex/rxjs';

export interface App {
  displayChannel: Observable<Board>
  exec(cmd: Command): Promise<void>
}

export module App {
  export function create(sitemapUrlOrInitialState:string|Promise<ApplicationState>): App {
    const state0 = typeof sitemapUrlOrInitialState === "string" ? initialState(sitemapUrlOrInitialState) : sitemapUrlOrInitialState;
    return new AppImpl(state0, applicationDispatch())
  }
}

class AppImpl implements App {

  displayChannel: Observable<Board>
  dispatcher: Promise<(cmd: Command) => Promise<ApplicationState>>
  private changesChannel= new ReplaySubject<ApplicationState>()

  constructor(initState:Promise<ApplicationState>, reducerFunction:(cmd: Command) => AsynchStateChange<ApplicationState,Command,Command>) {
    this.dispatcher = initState.then(state =>
      commandDispatcher(state, reducerFunction, (s) => this.changesChannel.next(s))
    )
    this.displayChannel = Observable.fromPromise(this.dispatcher).flatMap(d => this.changesChannel.map(s => s.board));

    this.displayChannel.subscribe(ev => {
      console.log("display channel ")
      console.log(ev)
      console.log("***************")
    }, err => {
      console.error(err)
    })
  }

  exec(cmd: Command): Promise<void> {
    return this.dispatcher.then(dispatch => {
      dispatch(cmd).catch(err => console.error(err))
    });
  }
}