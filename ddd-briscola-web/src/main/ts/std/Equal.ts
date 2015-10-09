module Std {

  export interface Equal<A> {
  
    equal(a:A, b:A):boolean
    
  }
  
  export class EqualLaw<A> {
    constructor(private eq:Equal<A>) {
    }
    commutative(a: A, b: A): boolean {
      return this.eq.equal(a, b) === this.eq.equal(b, a)
    }
    reflexive(a: A): boolean { 
      return this.eq.equal(a, a) 
    }
    transitive(a: A, b: A, c: A): boolean {
      const hyp = this.eq.equal(a, b) && this.eq.equal(b, c)
      if (hyp) return this.eq.equal(a, c)
      else return true
    }
  }
  
  export module Equal {
    
    export function make<A>(f:(a:A, b:A) => boolean):Equal<A> {
      return {
        equal:(a:A, b:A) => f(a,b)  
      }  
    }
    
    export const num = make<number>( (a,b) => a === b )
    export const bool = make<boolean>( (a,b) => a === b )
    export const str = make<string>( (a,b) => a === b )
    
    export function array<I>(eq:Equal<I>):Equal<I[]> {
      return make<I[]>( (a:I[], b:I[]) => {
        const len = a.length
        if (len !== b.length) return false;
        else {
          for (let i = 0; i < len; i++) {
            if (!eq.equal(a[i], b[i])) return false;
          }
          return true  
        }  
      })
    } 
    
  }
  
}