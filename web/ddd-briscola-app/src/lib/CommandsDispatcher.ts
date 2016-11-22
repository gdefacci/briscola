export default class CommandDispatcher<C,S> {
  private currentState:S
  private changesChannel= new Rx.ReplaySubject<S>()

  constructor(private select:(cmdId:C) => (state:S, exec:(c:C) => Promise<S>) => Promise<S>, initialState:S) {
    this.currentState = initialState
  }

  dispatch(command:C):Promise<S> {
    return this.select(command)(this.currentState, (cmd) => this.dispatch(cmd) ).then( newState => {
      this.currentState = newState
      this.changesChannel.onNext(newState)
      return newState
    })
  }

  changes():Rx.Observable<S> {
    return this.changesChannel
  }
}