import {Option, JsMap} from "flib"
import {JsConstructor, Selector, ByPropertySelector} from "rest-fetch"

export type Path = string

export interface DomainEvent {
  eventName: string
}

export type ByKindChoice = JsMap.Entry<JsConstructor<any>>

export function byKindChoice(bks:() => ByKindChoice[], desc:string):ByPropertySelector {
  const mp = JsMap.create(bks())
  return ByPropertySelector.fromEntries( (wso:{ kind:string }) => wso.kind, bks, desc)
}