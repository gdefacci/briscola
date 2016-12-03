export type StateChange<S,C,CMD> = (state: S, dispatch: (cmd: CMD) => Promise<S>) => S
export type AsynchStateChange<S,C,CMD> = (state: S, dispatch: (cmd: CMD) => Promise<S>) => Promise<S>

export type ReducerType<S,C, CMD> = (command: C) => StateChange<S,C, CMD>
export type AsynchReducerType<S,C, CMD> = (command: C) => AsynchStateChange<S,C, CMD>

export function synchReducer<S,C,CMD>(rt: ReducerType<S,C,CMD>): AsynchReducerType<S,C,CMD> {
  return (command: C) => synchStateChange(rt(command))
}
export function synchStateChange<S,C,CMD>(sc: StateChange<S,C,CMD>): AsynchStateChange<S,C,CMD> {
  return (st, d) => {
    try {
      const r = sc(st, d)
      return Promise.resolve(r)
    } catch (e) {
      return Promise.reject(e)
    }
  }
}
