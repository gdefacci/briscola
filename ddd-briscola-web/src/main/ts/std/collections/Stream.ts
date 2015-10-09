module Std.Collections {

  export interface Stream<T> {
    isEmpty(): boolean

    fold<R>(empty: () => R, cons: (head: T, rest: () => Stream<T>) => R): R

    forEach<R>(f: (t: T) => R): void

    map<R>(f: (t: T) => R): Stream<R>

    flatMap<R>(f: (t: T) => Stream<R>):Stream<R>

    filter(pred: (v: T) => boolean): Stream<T>

    foldLeft<R>(z: R, f: (z: R, i: T) => R): R
  }

  export module Stream {
    export function empty<T>(): Stream<T> {
      return new Empty<T>()
    }

    export function cons<T>(head: T, rest: () => Stream<T>) {
      return new Cons<T>(head, rest);
    }

    export function append<T>(a: Stream<T>, b: () => Stream<T>): Stream<T> {
      return a.fold<Stream<T>>(
        () => b(),
        (head, rest) => cons<T>(head, () => append(rest(), b))
      )
    }

    export function toArray<T>(str: Stream<T>): T[] {
      let res: T[] = []
      str.forEach(i => res.push(i))
      return res;
    }

    export function fromArray<T>(arr: T[]): Stream<T> {
      let acc = Stream.empty<T>()
      for (var i = arr.length - 1; i >= 0; i--) {
        acc = cons(arr[i], fix(acc))
      }
      return acc
    }

    export function monoid<A>() {
      return Std.monoid(Stream.empty<A>(), (f1, f2) => Stream.append(f1, () => f2))
    }

    export function reducer<A>() {
      return new Std.Reducer<A, Stream<A>>(
        monoid<A>(),
        (a: A) => Stream.cons<A>(a, () => Stream.empty<A>())
      )
    }
  }

  function fix<T>(a: T): () => T {
    return () => a
  }

  class Empty<T> implements Stream<T> {
    isEmpty() { return true }

    fold<R>(empty: () => R, cons: (head: T, rest: () => Stream<T>) => R): R {
      return empty();
    }

    map<R>(f: (t: T) => R): Stream<R> {
      return Stream.empty<R>()
    }

    flatMap<R>(f: (t: T) => Stream<R>) {
      return Stream.empty<R>()
    }

    filter(pred: (v: T) => boolean): Stream<T> {
      return this;
    }

    forEach<R>(f: (t: T) => R): void {
    }

    foldLeft<R>(z: R, f: (z: R, i: T) => R): R {
      return z;
    }
  }

  class Cons<T> implements Stream<T> {
    constructor(public head: T, public rest: () => Stream<T>) {
    }

    isEmpty() { return false }

    fold<R>(empty: () => R, cons: (head: T, rest: () => Stream<T>) => R): R {
      return cons(this.head, this.rest);
    }

    map<R>(f: (t: T) => R): Stream<R> {
      return Stream.cons(f(this.head), () => this.rest().map(f))
    }

    flatMap<R>(f: (t: T) => Stream<R>) {
      const prfx = f(this.head)
      return Stream.append(prfx, () => this.rest().flatMap(f))
    }

    filter(pred: (v: T) => boolean): Stream<T> {
      if (pred(this.head)) return Stream.cons(this.head, () => this.rest().filter(pred))
      else return this.rest().filter(pred)
    }

    forEach<R>(f: (t: T) => R): void {
      f(this.head)
      this.rest().forEach(f)
    }

    foldLeft<R>(z: R, f: (z: R, i: T) => R) {
      const z1 = f(z, this.head)
      return this.rest().foldLeft(z1, f);
    }
  }

}