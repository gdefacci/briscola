import {isNull, Option, JsMap} from "flib"
import {GameState, gameStateChoice} from "./game"
import {briscolaEventChoice, BriscolaEvent} from "./gameEvents"
import {competitionStateChoice, CompetitionState} from "./competition"
import {competitionEventChoice, CompetitionEvent} from "./competitionEvents"
import {playerEventChoice, Player, PlayerEvent} from "./player"
import {JsConstructor, convert, Converter, Selector, ByPropertySelector} from "rest-fetch"
import {ByKindChoice} from "./model"

export type RawEventAndState = { event:RawEvent }
export type RawEvent = { kind: string }

/*
export type RawGame = { kind:string }
export type RawCompetition = { kind:string }

export function byEventNameChoice<T>(desc:string, mp:JsMap<ConstructorType<any>>):(wso:RawEvent) => Option<ConstructorType<T>> {
  return (wso) => Option.option(mp[wso.kind])
}

const playerEventChoiceMap = JsMap.create(playerEventChoices())

// FIXME  make private
export const gameEventChoiceMap = JsMap.create(briscolaEventChoices())
// FIXME  make private
export const gameStateChoiceMap = JsMap.create(gameStateChoices())
const competitionEventChoiceMap = JsMap.create(competitionEventChoices())

export const playerEventChoice = Selector.create( byEventNameChoice("player event", playerEventChoiceMap) )
export const gameEventChoice = Selector.create( byEventNameChoice("game event", gameEventChoiceMap) )
export const competitionEventChoice = Selector.create( byEventNameChoice("competition event", competitionEventChoiceMap) )

export const gameStateChoice = Selector.create(
  (gm:RawGame) => Option.option(gameStateChoiceMap[gm.kind])
)
*/

export const eventAndStateChoice = Selector.create(
  (wso:RawEventAndState) => {
    const evName = wso.event && wso.event.kind
    if (playerEventChoice.contains(evName)) return Option.some(PlayerEventAndState)
    else if (briscolaEventChoice.contains(evName)) return Option.some(GameEventAndState)
    else if (competitionEventChoice.contains(evName)) return Option.some(CompetitionEventAndState)
    return Option.none<JsConstructor<any>>();
  })

export class PlayerEventAndState {
  @convert(playerEventChoice)
  event:PlayerEvent

  @convert({ arrayOf:Player }, { property:"state" })
  players:Player[]
}

export class GameEventAndState {
  @convert(briscolaEventChoice)
  event:BriscolaEvent

  @convert(gameStateChoice, {property:"state"})
  game:GameState
}

export class CompetitionEventAndState {
  @convert(competitionEventChoice)
  event:CompetitionEvent

  @convert(CompetitionState, {property:"state"})
  competition:CompetitionState
}

export type EventAndState = PlayerEventAndState | GameEventAndState | CompetitionEventAndState