module Std {
  
  export module Disj {
    export function left<L,R>(l:L):Disj<L,R> {
      return new DisjImpl<L,R>(some(l), Std.none<R>())
    }
    
    export function right<L,R>(r:R):Disj<L,R> {
      return new DisjImpl<L,R>(Std.none<L>(), some(r))
    }
  }
  
  export interface Disj<L,R> {
    isLeft():boolean
    isRight():boolean
    flatMap<R1>(f:(r:R) => Disj<L, R1>):Disj<L,R1>
    leftFlatMap<L1>(f:(r:L) => Disj<L1, R>):Disj<L1,R> 
    map<R1>(f:(r:R) => R1):Disj<L,R1>
    leftMap<L1>(f:(r:L) => L1):Disj<L1,R>
    leftOption:Option<L>
    rightOption:Option<R>
  }
  
  class DisjImpl<L,R> implements Disj<L,R> {
    constructor(public leftOption:Option<L>, public rightOption:Option<R>){
    }
    map<R1>(f:(r:R) => R1):Disj<L,R1> {
        return this.rightOption.fold(
          () => new DisjImpl<L,R1>(this.leftOption, none<R1>()),
          (r) => new DisjImpl<L,R1>(none<L>(), some(f(r)))
        )
    }
    leftMap<L1>(f:(r:L) => L1):Disj<L1,R> {
        return this.leftOption.fold(
          () => new DisjImpl<L1,R>(none<L1>(), this.rightOption),
          (l) => new DisjImpl<L1,R>(some(f(l)), none<R>())
        )
    }
    isLeft() {
      return this.leftOption.isDefined()   
    }
    isRight() {
      return this.rightOption.isDefined()   
    }
    flatMap<R1>(f:(r:R) => Disj<L, R1>):Disj<L,R1> {
      return this.rightOption.fold(
          () => new DisjImpl<L,R1>(this.leftOption, none<R1>()),
          (r) => f(r)
        )  
    }
    leftFlatMap<L1>(f:(r:L) => Disj<L1, R>):Disj<L1,R> {
        return this.leftOption.fold(
          () => new DisjImpl<L1,R>(none<L1>(), this.rightOption),
          (l) => f(l)
        )
    }  
  }
  

}