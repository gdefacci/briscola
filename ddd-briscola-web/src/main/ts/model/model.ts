namespace Model {

  import Option = Std.Option
  import JsMap = Std.JsMap

  export type Player = Ws.Player

  export class DomainEvent {
    constructor(public eventName: string) {
    }
  }

  export class Aggregate {
    constructor() {
    }
  }

}