module Std {

  export enum OrdResult {
    lt = -1,
    eq = 0,
    gt = 1
  }
  
  export interface Ord<T> {
   (a:T, b:T):OrdResult  
  }
  
  export function reveseOrder<T>(ord:Ord<T>):Ord<T> {
    return (a:T, b:T):OrdResult => {
      return ord(b,a)  
    }
  }

}