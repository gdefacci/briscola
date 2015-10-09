module Std {

  export module Tuples {

    export function _2<A, B>(a: A, b: B): [A, B] {
      return [a, b];
    }

    export function _3<A, B, C>(a: A, b: B, c: C): [A, B, C] {
      return [a, b, c];
    }

  }

}