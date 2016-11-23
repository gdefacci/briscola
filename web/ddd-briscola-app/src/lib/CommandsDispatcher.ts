export default function commandDispatcher<C, S>(
  changesChannel: Rx.ReplaySubject<S>,
  dispatchFuntion: (cmdId: C) => ((state: S, dispatch: (cmd: C) => Promise<S>) => Promise<S>),
  initialState: S): (cmd: C) => Promise<S> {

  let currentState: S = initialState
  const dispatch: (cmd: C) => Promise<S> = (cmd: C) => {
    const reducer = dispatchFuntion(cmd)
    return reducer(currentState, dispatch).then(newState => {
      currentState = newState
      changesChannel.onNext(newState)
      return newState
    })
  }
  return dispatch;
}