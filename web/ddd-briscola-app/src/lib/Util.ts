import { Option, isNull } from "flib"

export module Http {

  function fetchWithBody<B>(mthd: string): (url: string, body?: B) => Promise<Response> {
    return (url, body) => {
      if (url === undefined || url === null) throw new Error("making request at null url, body : "+body)

      const bdy = (body !== undefined && body !== null) ? JSON.stringify(body) : undefined
      return window.fetch(url, {
        method: mthd,
        body: bdy
      })
    }
  }

  export function POST<B>(url: string, body?: B): Promise<Response> {
    return fetchWithBody<B>("POST")(url, body)
  }

  export function PUT<B>(url: string, body?: B): Promise<Response> {
    return fetchWithBody<B>("PUT")(url, body)
  }

  export function DELETE(url: string): Promise<Response> {
    return window.fetch(url, {
      method: "DELETE"
    })
  }

  function parseJson(txt: string): Promise<any> {
    try {
      return Promise.resolve(JSON.parse(txt))
    } catch (e) {
      return Promise.reject(`Error parsing json ${e.message}`)
    }
  }

  function statusOk(resp: Response): boolean {
    const st = resp.status
    return (st >= 200 && st < 300);
  }

  function reqDesc(req: Request): string {
    return `${req.method} ${req.url}`
  }

  export const jsonResponseReader = (resp: Response, req: Request): Promise<any> => {
    if (statusOk(resp)) {
      return resp.text().then(parseJson)
    } else {
      return Promise.reject(`${reqDesc(req)} return status : ${resp.status}`)
    }
  }

  export function createRequestFactory(reqInit: RequestInit): (url:string) => Request {
    return (url:string) => {
      return new Request(url, reqInit);
    }
  }

}

export function observableWebSocket(url: string, protocol?: string | string[]): Rx.Observable<MessageEvent> {
  if (isNull(url)) throw new Error("websocket error, url is undefined")
  if (isNull(WebSocket)) throw new Error("websocket error, WebSocket not supported in current environment")

  const channel = new Rx.Subject<MessageEvent>();
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
    channel.onNext(e)
  }

  return channel;
}

/*
function observableWebSocket1(url:string, protocol?: string | string[]): Rx.Observable<MessageEvent> {
  const channel = new Rx.Subject<MessageEvent>()
  const socket = protocol === undefined ? new WebSocket(url, protocol) : new WebSocket(url);

  function showEvent(e:Event) {
    console.log("receive from websocket")
    console.log(e)
  }

  socket.onclose = (e) => {
    showEvent(e)
    channel.onCompleted()
  }

  socket.onerror = (e) => {
    showEvent(e)
    channel.onError(e)
  }

  socket.onopen = showEvent

  socket.onmessage = e => {
    showEvent(e)
    channel.onNext(e)
  }

  return channel;
}
*/

export function rxCollect<T, R>(obs: Rx.Observable<T>, pf: (v: T) => Option<R>): Rx.Observable<R> {
  return obs.flatMap(v =>
    pf(v).fold(
      () => Rx.Observable.empty<R>(),
      (v) => Rx.Observable.just<R>(v)
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
