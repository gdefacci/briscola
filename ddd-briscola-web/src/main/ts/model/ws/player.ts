namespace Model.Ws {

  export type PlayerEvent = PlayerLogOn | PlayerLogOff

  export interface PlayerLogOn {
    player: Path
    kind: string
  }

  export interface PlayerLogOff {
    player: Path
    kind: string
  }

  export interface Player {
    self: Path
    name: string
    webSocket?: Path
    createCompetition?: Path
  }

  const playerEvents: Std.JsMap<boolean> = {
    playerLogOn: true,
    playerLogOff: true
  }


  export function isPlayerEvent(str: string) {
    return playerEvents[str] === true;
  }

}