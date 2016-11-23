import { Command } from "./Command"
import { Board } from "ddd-briscola-model"
import commandDispatcher from "./CommandsDispatcher"
import { ApplicationState, initialState } from "./ApplicationState"
import applicationDispatch  from "./ApplicationDispatch"
import * as Reducers from "./Reducers"

export interface App {
  displayChannel: Rx.Observable<Board>
  exec(cmd: Command): void
}

export module App {
  export function create(entryPoint: string): App {
    return new AppImpl(entryPoint, applicationDispatch())
  }
}

class AppImpl implements App {

  displayChannel: Rx.Observable<Board>
  dispatcher: Promise<(cmd: Command) => Promise<ApplicationState>>
  private changesChannel= new Rx.ReplaySubject<ApplicationState>()

  constructor(entryPoint: string, reducerFunction:(cmd: Command) => Reducers.AsynchStateChange) {
    const initState = initialState(entryPoint)
    this.dispatcher = initState.then(state =>
      commandDispatcher(this.changesChannel, reducerFunction, state)
    )
    this.displayChannel = Rx.Observable.fromPromise(this.dispatcher).flatMap(d => this.changesChannel.map(s => s.board));
    this.displayChannel.subscribe(ev => {
      console.log("display channel ")
      console.log(ev)
      console.log("***************")
    }, err => {
      console.error(err)
    })
  }

  exec(cmd: Command): void {
    this.dispatcher.then(dispatch => {
      dispatch(cmd).catch(err => console.error(err))
    });
  }
}