import { Arrays } from "flib"
import { GameState, gameStateChoice } from "./game"
import { briscolaEventChoice, BriscolaEvent, gameEvents } from "./gameEvents"
import { CompetitionState } from "./competition"
import { competitionEventChoice, CompetitionEvent, competitionEvents } from "./competitionEvents"
import { playerEventChoice, Player, PlayerEvent, playerEvents } from "./player"
import { convert, mapping, ChoiceValue, arrayOf, Lazy } from "nrest-fetch"

export type RawEventAndState = { event: RawEvent }
export type RawEvent = { kind: string }

export const eventAndStateChoice:() => ChoiceValue<any> = Lazy.choose("EventAndState", [
    wso => wso.event && eventMatch(playerEvents, wso.event),
    () => PlayerEventAndState
  ], [
    wso => wso.event && eventMatch(gameEvents, wso.event),
    () => GameEventAndState
  ], [
    wso => wso.event && eventMatch(competitionEvents, wso.event),
    () => CompetitionEventAndState
  ])


function eventMatch<T>(preds: [(a: any) => boolean, T][], a: any): boolean {
  return Arrays.exists(preds, (p) => p[0](a))
}

export class PlayerEventAndState {
  @convert(playerEventChoice)
  event: PlayerEvent

  @convert(arrayOf(Player), "state")
  players: Player[]
}

export class GameEventAndState {
  @convert(briscolaEventChoice)
  event: BriscolaEvent

  @convert(gameStateChoice, "state")
  game: GameState
}

export class CompetitionEventAndState {
  @convert(competitionEventChoice)
  event: CompetitionEvent

  @convert(mapping(CompetitionState), "state")
  competition: CompetitionState
}

export type EventAndState = PlayerEventAndState | GameEventAndState | CompetitionEventAndState