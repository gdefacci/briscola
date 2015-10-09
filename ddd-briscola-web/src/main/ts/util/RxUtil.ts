module Util.RxUtil {

  import AnonymousObservable = Rx.AnonymousObservable
  import Observer = Rx.Observer
  
  let lastMessageSent: number = undefined
  const webSocketPingIntervalSeconds = 5
  const taskFrequencySeconds = 2
  
  const pingMessage = "PING"
  const pongMessage = "PONG"
  
  interface Defer<T> {
    promise:Promise<T>
    resolve(t:T):void  
    reject(err:any):void
  }
  
  function defer<T>():Defer<T> {
    var result = <Defer<T>>{};
    result.promise = new Promise(function(resolve, reject) {
        result.resolve = resolve;
        result.reject = reject;
    });
    return result;
  };
  
  export function webSocketObservable(url:string, protocol?: string | string[]): Rx.Observable<MessageEvent> {
    const channel = new Rx.Subject<MessageEvent>()
    const socket = !Std.isNull(protocol) ? new WebSocket(url, protocol) : new WebSocket(url);

    function showEvent(e:Event) {
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
  
  
  export function webSocketObservable_old(url:string, protocol?: string | string[], openObserver?: Rx.Observer<Event>, closingObserver?: Rx.Observer<void>): Rx.Observable<MessageEvent> {
    if (!WebSocket) { throw new TypeError('WebSocket not implemented in your runtime.'); }

    let socket: WebSocket;

    function socketClose(code?: number, reason?: string) {
      if (socket) {
        if (closingObserver) {
          closingObserver.onNext(undefined);
          closingObserver.onCompleted();
        }
        if (!code) {
          socket.close();
        } else {
          socket.close(code, reason);
        }
      }
    }

    var observable = new AnonymousObservable<MessageEvent>(function(obs: Observer<MessageEvent>) {
      socket = protocol ? new WebSocket(url, protocol) : new WebSocket(url);

      const pingTaskId = setInterval(
        function() {
          var interval = webSocketPingIntervalSeconds * 1000;
          var currentTime = new Date().getTime();
          if (!lastMessageSent || lastMessageSent < (currentTime - interval)) {
            socket.send(pingMessage);
            lastMessageSent = currentTime;
            console.log("sent ping message")
          }
        },
        taskFrequencySeconds * 1000
      );
      
      const openHandler = (e: Event) => {
        if (openObserver !== undefined) {
          openObserver.onNext(e);
          openObserver.onCompleted();
        }
        socket.removeEventListener('open', openHandler, false);
      }
      const messageHandler = (e: MessageEvent) => {
        if (e.data !== pongMessage) {
          obs.onNext(e);
          console.log("webscoket received message event")
        } else {
          console.log("webscoket received pong message")          
          console.log(e)  
        }
      }
      const errHandler = (e: ErrorEvent) => obs.onError(e);
      const closeHandler = (e: CloseEvent) => {
        console.log("close event")
        console.log(e)
        if (e.code !== 1000 || !e.wasClean) { return obs.onError(e); }
        obs.onCompleted();
      }

      openObserver && socket.addEventListener('open', openHandler, false);
      socket.addEventListener('message', messageHandler, false);
      socket.addEventListener('error', errHandler, false);
      socket.addEventListener('close', closeHandler, false);

      return function() {
        socketClose();

        clearInterval(pingTaskId)
        socket.removeEventListener('message', messageHandler, false);
        socket.removeEventListener('error', errHandler, false);
        socket.removeEventListener('close', closeHandler, false);
      };
    });

    return observable
  }


}