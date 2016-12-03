import { Option, isNull } from "flib"
import { Observable, Subject } from '@reactivex/rxjs';

export function observableWebSocket(url: string, protocol?: string | string[]): Observable<MessageEvent> {
  if (isNull(url)) throw new Error("websocket error, url is undefined")
  if (isNull(WebSocket)) throw new Error("websocket error, WebSocket not supported in current environment")

  const channel = new Subject<MessageEvent>();
  const socket = !isNull(protocol) ? new WebSocket(url, protocol) : new WebSocket(url);

  function showEvent(e: Event) {
    console.log("receive from websocket")
    console.log(e)
  }

  socket.onclose = showEvent
  socket.onerror = showEvent
  socket.onopen = showEvent
  socket.onmessage = e => {
    showEvent(e)
    channel.next(e)
  }

  return channel;
}

export function rxCollect<T, R>(obs: Observable<T>, pf: (v: T) => Option<R>): Observable<R> {
  return obs.flatMap(v =>
    pf(v).fold(
      () => Observable.empty<R>(),
      (v) => Observable.of<R>(v)
    )
  )
}

/*
* adapted from https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Object/assign
*/
export function objectAssign<A, B>(target: A, source: B): A & B {
  if (target === undefined || target === null) {
    throw new TypeError('Cannot convert undefined or null to object');
  }

  const output = Object(target);
  if (source !== undefined && source !== null) {
    for (const nextKey in source) {
      if (source.hasOwnProperty(nextKey)) {
        output[nextKey] = source[nextKey];
      }
    }
  }
  return output;
}

export function copy<A, B>(a: A, b: B): A & B {
  return objectAssign(objectAssign({}, a), b);
}