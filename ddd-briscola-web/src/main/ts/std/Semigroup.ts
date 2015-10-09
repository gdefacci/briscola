module Std {
  
  export interface Semigroup<A> {
    append(a:A, b:A):A  
  }
  
  export interface Monoid<A> {
    zero:A
    append(a:A, b:A):A  
  }

  export function semigroup<A>(f:(a:A, b:A) => A):Semigroup<A> {
    return {
      append(a:A, b:A):A {
        return f(a,b)  
      }  
    }  
  }
  
  export function monoid<A>(zero:A, f:(a:A, b:A) => A):Monoid<A> {
    return {
      zero:zero,
      append(a:A, b:A):A {
        return f(a,b)  
      }  
    }  
  }
  
  export class Reducer<C,M> {
    constructor(public monoid:Monoid<M>, public unit:(c:C) => M) {
    }  
    append(a1: M, a2: M): M {
      return this.monoid.append(a1, a2)
    }
    composef<B>(f:(b:B) => C) {
      return new Reducer<B,M>(this.monoid, b => this.unit(f(b)))  
    }
    snoc(m: M, c: C): M {
      return this.monoid.append(m, this.unit(c))  
    }
    cons(c: C, m: M): M {
      return this.monoid.append(this.unit(c), m)  
    }
    compose<N>(r: Reducer<C, N>): Reducer<C, [M, N]> {
      const resMonoid = monoid<[M,N]>([this.monoid.zero, r.monoid.zero], (a:[M,N],b:[M,N]):[M,N] => {
        return [this.monoid.append(a[0],b[0]), r.monoid.append(a[1],b[1])] 
      } )
      return new Reducer<C, [M, N]>(resMonoid, (c:C):[M, N] => {
        return [this.unit(c), r.unit(c)]
      }) 
    }
  }
  
  export class SemigroupLaw<A> {
    constructor(public semigroup: Semigroup<A>, public eq: Equal<A>) {
    }
    associative(f1: A, f2: A, f3: A): boolean {
      return this.eq.equal(this.semigroup.append(f1, this.semigroup.append(f2, f3)), this.semigroup.append(this.semigroup.append(f1, f2), f3))
    }
  }
  
  export class MonoidLaw<A> extends SemigroupLaw<A> {
    constructor(public monoid: Monoid<A>, eq: Equal<A>) {
      super(monoid, eq)
    }
    leftIdentity(a: A) {
      this.eq.equal(a, this.monoid.append(this.monoid.zero, a))
    }
    rightIdentity(a: A) {
      this.eq.equal(a, this.monoid.append(a, this.monoid.zero))
    }
  }

  export module Monoid {
    export const num = monoid<number>(0, (a,b) => a + b)
  }
  
}