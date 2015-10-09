module Std {
  export interface JsMap<A> {
      [s:string]:A  
  }
  
  interface KeyValue<A> {
    key:string
    value:A
  }
  
  export function jsMap<A>(es:KeyValue<A>[]) {
    return jsMapSetValues({}, es)
  }
  export function jsMapSetValues<A>(mp:JsMap<A>, es:KeyValue<A>[]) {
    es.forEach( e => {
      mp[e.key] = e.value;
    })
    return mp;
  }
  
  export function jsMapSetAll<A>(mp:JsMap<A>, ks:string[], v:A) {
    ks.forEach( k => {
      mp[k] = v;
    })
    return mp;
  }
  
  interface StringMapping<T> {
    (k:string):T
    opt:(k:string) => Option<T>
    toOptString(v:T):Option<string>
  }
  
//  export function reverseIndexMapping<T>(mp:JsMap<T>, idx:(v:T) => number):(k:T) => string {
//    const arr:string[] = []
//    Object.keys(mp).forEach( k => {
//      const v = mp[k]
//      if (v !== undefined) {
//        arr[idx(v)] = k  
//      }
//    })
//    return v => {
//      const i = idx(v)
//      return arr[i];  
//    }
//  }
 
  export function stringMapping<T>(mp:JsMap<T>, dflt?:() => T):StringMapping<T> {
    const f = optStringMapping(mp)
    const r:StringMapping<T> = <any>( (k:string) => f(k).getOrElse( () => {
        if (Std.isNull(dflt)) throw new Error(`missing key `+k)
        else return dflt()
    }))
    r.opt = f;
    return r;
  }
  export function optStringMapping<T>(mp:JsMap<T>):(k:string) => Option<T> {
    return k => {
      const v = mp[k];
      if (Std.isNull(v)) return Std.none<T>();  
      else return Std.some<T>(v);  
    }
  }
}