export default function commandDispatcher<C, S>(
  initialState: S,
  dispatchFuntion: (cmdId: C) => ((state: S, dispatch: (cmd: C) => Promise<S>) => Promise<S>),
  effect: (s: S) => void): (cmd: C) => Promise<S> {

  let currentState: S = initialState
  const dispatch: (cmd: C) => Promise<S> = (cmd: C) => {
    const reducer = dispatchFuntion(cmd)
    return reducer(currentState, dispatch).then(newState => {
      currentState = newState
      effect(newState)
      return newState
    })
  }
  return dispatch;
}