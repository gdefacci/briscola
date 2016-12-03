import "reflect-metadata"
import 'isomorphic-fetch'

import { Observable } from '@reactivex/rxjs';
import { ConfigMap, TestClient } from "./TestHttpClient"
import { Option, JsMap } from "flib"
import { ResourceFetch, ExtraPropertiesStrategy } from "nrest-fetch"
import { createInitialState } from "../lib/ApplicationState"
import { App } from "../lib/App"

export class ObservableBuffer<T> {
  readonly buffer: T[] = []
  constructor(obs: Observable<T>) {
    obs.subscribe(v => {
      this.buffer.push(v)
    })
  }
}

function failTest(done?: () => void): (error: Error) => void {
  return (error) => {
    if (error !== undefined) {
      console.log(error["stack"])
      fail(error)
    }
    if (done) done()
  }
};

export function expectResolve<T>(p: Promise<T>): (onSuccess: (v: T) => void, done?: () => void) => void {
  return (onSuccess, done) =>
    p.then((v) => {
      try {
        onSuccess(v)
      } catch (e) {
        fail(e)
      }
      if (done) done()
    }).catch(failTest(done))
}

export interface TestConfigMap extends ConfigMap {
  GET?: JsMap<any>
}

export function createClients(cfg:TestConfigMap):[ResourceFetch, TestClient] {
  const rf = new ResourceFetch(ExtraPropertiesStrategy.copy, () => (url:string) => Promise.resolve(Option.option(cfg.GET && cfg.GET[url])))
  const client = new TestClient(cfg)
  return [rf, client]
}

export function createApplication(cfg: TestConfigMap, webSocket?: Observable<MessageEvent>) {
  const [rf, testClient] = createClients(cfg)
  return App.create(createInitialState(rf, testClient, webSocketFactory(webSocket), "/entrypoint"))
}

export function webSocketFactory(ch?: Observable<MessageEvent>): (url: string) => Observable<MessageEvent> {
  if (ch === null || ch === undefined) return (url: string) => Observable.empty()
  else return (url: string) => ch
}

export function asserThatObservable<T>(ch:Observable<T>, timeout:number = 200):(pred:(res:T[]) => void) => void {
    return pred => {
      const out = new ObservableBuffer(ch)
      const sub = ch.finally(() => {
        pred(out.buffer)
      }).subscribe()
      setTimeout( () => sub.unsubscribe(), timeout )
    }
}