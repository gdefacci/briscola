module Model {

  export enum PlayerEventKind {
    playerLogOn, playerLogOff
  }

  export type PlayerEvent = PlayerLogOn | PlayerLogOff

  export class PlayerLogOn extends DomainEvent {
    constructor(public player: Player) {
      super(PlayerEventKind[PlayerEventKind.playerLogOn])
    }
  }

  export class PlayerLogOff extends DomainEvent {
    constructor(public player: Player) {
      super(PlayerEventKind[PlayerEventKind.playerLogOff])
    }
  }

  export module PlayerEvent {
    export function fold<T>(
      playerLogOn: (p: PlayerLogOn) => T, playerLogOff: (p: PlayerLogOff) => T): (p: PlayerEvent) => T {
      return p => {
        if (p instanceof PlayerLogOn) return playerLogOn(p)
        else if (p instanceof PlayerLogOff) return playerLogOff(p)
        else {
          console.log("unrecognized PlayerEvent")
          console.log(p)
          return Util.fail<T>("unrecognized PlayerEvent ")
        }
      }
    }
  }

}