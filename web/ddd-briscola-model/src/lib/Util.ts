import {JsMap, isNull} from "flib"

export function toEnum<T>(a: any, desc: string): (str: String) => T {
  return valueFrom<T>(<any>a, desc);
}

export function valueFrom<T>(mp: JsMap<T>, desc: string): (str: String) => T {
  return (str: string) => {
    const r = mp[str];
    if (isNull(r)) throw Error(`${str} is not a ${desc}`)
    return r;
  }
}
