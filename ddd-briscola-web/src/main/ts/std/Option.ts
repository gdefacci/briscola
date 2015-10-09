module Std {

  export interface Option<A> {
    fold<B>(fnone: () => B, fsome: (v: A) => B): B
    map<B>(f: (t: A) => B): Option<B>
    forEach<B>(f: (t: A) => void): void
    flatMap<B>(f: (t: A) => Option<B>): Option<B>
    getOrElse(f: () => A): A
    orElse(f: () => Option<A>): Option<A>
    isEmpty(): boolean
    isDefined(): boolean
    zip<A>(b: Option<A>): Option<[A, A]>
    toArray(): A[]
  }

  export interface Some<T> extends Option<T> {
    value: T
  }

  export interface None<T> extends Option<T> {
  }

  const NoneImpl = {
    isEmpty(): boolean { return true },
    isDefined(): boolean { return false },
    fold<B>(fnone: () => B, fsome: (some: any) => B): B {
      return fnone();
    },
    forEach<B>(f: (a: any) => void): void {
    },
    map<B>(f: (a: any) => B): Option<B> {
      return NoneImpl;
    },
    flatMap<B>(f: (a: any) => Option<B>): Option<B> {
      return NoneImpl;
    },
    getOrElse(f: () => any): any {
      return f()
    },
    zip<B>(b: Option<B>): None<any> {
      return NoneImpl
    },
    orElse(f: () => Option<any>): Option<any> {
      return f()
    },
    toArray(): any[] {
      return [];
    },
    toString(): string {
      return "None";
    }
  }

  class SomeImpl<A> {
    constructor(public value: A) { }
    isEmpty(): boolean { return false }
    isDefined(): boolean { return true }
    fold<B>(fnone: () => B, fsome: (some: A) => B): B {
      return fsome(this.value)
    }
    forEach<B>(f: (a: A) => void): void {
      f(this.value)
    }
    map<B>(f: (a: A) => B): Option<B> {
      return some(f(this.value))
    }
    flatMap<B>(f: (a: A) => Option<B>): Option<B> {
      return f(this.value)
    }
    getOrElse(f: () => A): A {
      return this.value;
    }
    orElse(f: () => Option<A>): Option<A> {
      return this;
    }
    zip<B>(b: Option<B>): Option<[A, B]> {
      return b.map(t1 => Tuples._2(this.value, t1))
    }
    toArray(): A[] {
      return [this.value]
    }
    toString(): string {
      return `Some(${this.value})`
    }
  }

  export function some<T>(v: T): Option<T> {
    return new SomeImpl<T>(v)
  }

  export function none<T>(): Option<T> {
    return NoneImpl;
  }

  export function option<T>(t: T): Option<T> {
    return (!isNull(t)) ? some<T>(t) : none<T>();
  }
}