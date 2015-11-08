import {Option, JsMap} from "flib"
import {ConstructorType, ChooseConverter, ByPropertyChooseConverter} from "rest-fetch"

export type Path = string

export interface DomainEvent {
  eventName: string
}

export function byKindChoice_old<T>(mp:JsMap<ConstructorType<T>>, desc:string):(wso:{ kind:string }) => ConstructorType<T> {
  return (wso) => {
    const r = mp[wso.kind];
    if (r === undefined) throw new Error(`invalid ${desc} : ${JSON.stringify(wso)}`)
    return r;
  }
}

export type ByKindChoice = JsMap.Entry<ConstructorType<any>>

export function byKindChoice_old1(bks:() => ByKindChoice[]):ChooseConverter {
  const mp = JsMap.create(bks())
  return ChooseConverter.create((wso:{ kind:string }) => Option.option(mp[wso.kind]))
}

export function byKindChoice(bks:() => ByKindChoice[], desc:string):ByPropertyChooseConverter {
  const mp = JsMap.create(bks())
  return ByPropertyChooseConverter.fromEntries( (wso:{ kind:string }) => wso.kind, bks, desc)
}