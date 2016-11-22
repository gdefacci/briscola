import { Command } from "./Command"
import { Board } from "ddd-briscola-model"
import CommandDispatcher from "./CommandsDispatcher"
import { ApplicationState, initialState } from "./ApplicationState"
import { ApplicationDispatch } from "./ApplicationDispatch"

export interface App {
  displayChannel: Rx.Observable<Board>
  exec(cmd: Command): void
}

export module App {
  export function create(entryPoint: string): App {
    return new AppImpl(entryPoint)
  }
}

class AppImpl implements App {

  displayChannel: Rx.Observable<Board>
  dispatcher: Promise<CommandDispatcher<Command, ApplicationState>>
  constructor(entryPoint: string) {
    const initState = initialState(entryPoint)
    this.dispatcher = initState.then(state =>
      new CommandDispatcher<Command, ApplicationState>(ApplicationDispatch, state)
    )
    this.displayChannel = Rx.Observable.fromPromise(this.dispatcher).flatMap(d => d.changes().map(s => s.board));
    this.displayChannel.subscribe(ev => {
      console.log("display channel ")
      console.log(ev)
      console.log("***************")
    }, err => {
      console.error(err)
    })
  }

  exec(cmd: Command): void {
    this.dispatcher.then(d => {
      d.dispatch(cmd).catch(err => console.error(err))
    });
  }
}