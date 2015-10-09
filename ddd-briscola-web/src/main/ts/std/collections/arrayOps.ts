module Std.Collections {

  export module Arrays {

    export function flatMap<A,B>(arr:A[], f:(a:A) => B):B[] {
        let res:B[] = [];
        arr.forEach( a => res = res.concat(f(a)))
        return res;
    }
    
    export function find<A>(arr:A[], pred:(a:A) => boolean):Option<A> {
        for (let i = 0; i < arr.length; i++) {
          const v = arr[i]
          if (pred(v)) return option(v)
        }
        return none<A>();
    }
    
     export function exists<A>(arr:A[], pred:(a:A) => boolean):boolean {
        for (let i = 0; i < arr.length; i++) {
          const v = arr[i]
          if (pred(v)) return true
        }
        return false;
    }
    
    export function foldLeft<A,B>(arr:A[], z:B, f:(acc:B,i:A) => B):B {
      let res = z
      arr.forEach( i => res = f(res,i))
      return res
    }
    
    export function foldRight<A,B>(arr:A[], z:B, f:(i:A, acc:B) => B):B {
      let res = z
      const len = arr.length
      for (let idx = len-1; idx >= 0; idx--) {
        res = f(arr[idx], res)
      }
      return res
    }
    
    export function monoid<A>() {
      return Std.monoid<A[]>([], (a,b) => a.concat(b))  
    }
 
    export function reducer<A>() {
      return new Std.Reducer<A, A[]>(monoid<A>(), (a) => [a] )
    }
  }

}