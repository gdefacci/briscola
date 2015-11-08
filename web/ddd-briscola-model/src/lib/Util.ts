import {Arrays} from "flib"

export function asStringArray(v:any):Promise<string[]> {
  if (Array.isArray(v)) {

    const nonStringItemIdx = Arrays.findIndex(v, v => typeof v !== "string")
    return nonStringItemIdx.fold<Promise<string[]>>(
      () =>  
        Promise.resolve<string[]>(<any>v),
      (idx) => {
        const t = v[idx] 
        return Promise.reject(`item at index ${t[1]} of array is not a string :${t[0]}, array : [${v.map(JSON.stringify).join(", ")}]`)
      }
    )
  } else {
    return Promise.reject(`not an array of string :${JSON.stringify(v)}`)
  }
}