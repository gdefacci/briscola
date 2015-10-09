module Std {
  
  export interface Constructible<T> {
    new(... args:any[]):T
  }
  
  export function jsClassName<T>(c:Constructible<T>):string {
    return c["name"];
  }  
  
  export function classNameOf(c:any):string {
    return c["constructor"]["name"];
  }  
  
  export function nameOfSuperClasses(c:any):string[] {
    let currProto = c["__proto__"];
    if (isNull(currProto)) return []
    else currProto = currProto["__proto__"]
    const res = []
    while (!isNull(currProto)) {
      res.push(classNameOf(currProto))
      currProto = currProto["__proto__"];  
    }   
    return res;
  }
  
  export class ByClassMap<T> {
    private map:JsMap<T> = {}

    put(cmd:Constructible<any>, v:T) {
      this.map[jsClassName(cmd)] = v
    }
    
    byClassOf(cmd:any):Option<T> {
      const dispatchRes = this.map[classNameOf(cmd)]
      if (dispatchRes !== undefined) return some(dispatchRes);
      else {
        const scs = nameOfSuperClasses(cmd);
        let res:T = undefined;
        for (let i=0; i < scs.length && res === undefined; i++) {
            res = this.map[scs[i]]
        }
        return option(res);
      }
    }
    
  }
 
  export class DispatchTable<A,B> {
    dispatchTable = new ByClassMap<(a: A) => B>()
    constructor(private defaultValue:(a:A) => B) {
    }
    put<T extends A>(cmdClass: Constructible<T>, action: (v: T) => B) {
      this.dispatchTable.put(cmdClass, a => action(<T>a));
    }  
    dispatch(a: A): B {
      const dispatchRes = this.dispatchTable.byClassOf(a).getOrElse(() => undefined)
      if (dispatchRes !== undefined) {
        return dispatchRes(a);
      } else {
        return this.defaultValue(a);
      }
    }
  }

}