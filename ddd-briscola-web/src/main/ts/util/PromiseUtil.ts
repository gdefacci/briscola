module Util {

  export module TPromise {
    
    export function all2<T1, T2>(p1:Promise<T1>, p2:Promise<T2>):Promise<[T1, T2]> {
      return Promise.all<any>( [p1, p2] );
    }

    export function all3<T1, T2, T3>(p1:Promise<T1>, p2:Promise<T2>, p3:Promise<T3>):Promise<[T1, T2, T3]> {
      return Promise.all<any>( [p1, p2, p3] );
    }

    export function all4<T1, T2, T3, T4>(p1:Promise<T1>, p2:Promise<T2>, p3:Promise<T3>, p4:Promise<T4>):Promise<[T1, T2, T3, T4]> {
      return Promise.all<any>( [p1, p2, p3, p4] );
    }

    export function all5<T1, T2, T3, T4, T5>(p1:Promise<T1>, p2:Promise<T2>, p3:Promise<T3>, p4:Promise<T4>, p5:Promise<T5>):Promise<[T1, T2, T3, T4, T5]> {
      return Promise.all<any>( [p1, p2, p3, p4, p5] );
    }

    export function all6<T1, T2, T3, T4, T5, T6>(p1:Promise<T1>, p2:Promise<T2>, p3:Promise<T3>, p4:Promise<T4>, p5:Promise<T5>, p6:Promise<T6>):Promise<[T1, T2, T3, T4, T5, T6]> {
      return Promise.all<any>( [p1, p2, p3, p4, p5, p6] );
    }

    export function all7<T1, T2, T3, T4, T5, T6, T7>(p1:Promise<T1>, p2:Promise<T2>, p3:Promise<T3>, p4:Promise<T4>, p5:Promise<T5>, p6:Promise<T6>, p7:Promise<T7>):Promise<[T1, T2, T3, T4, T5, T6, T7]> {
      return Promise.all<any>( [p1, p2, p3, p4, p5, p6, p7] );
    }

    export function all8<T1, T2, T3, T4, T5, T6, T7, T8>(p1:Promise<T1>, p2:Promise<T2>, p3:Promise<T3>, p4:Promise<T4>, p5:Promise<T5>, p6:Promise<T6>, p7:Promise<T7>, p8:Promise<T8>):Promise<[T1, T2, T3, T4, T5, T6, T7, T8]> {
      return Promise.all<any>( [p1, p2, p3, p4, p5, p6, p7, p8] );
    }

    export function all9<T1, T2, T3, T4, T5, T6, T7, T8, T9>(p1:Promise<T1>, p2:Promise<T2>, p3:Promise<T3>, p4:Promise<T4>, p5:Promise<T5>, p6:Promise<T6>, p7:Promise<T7>, p8:Promise<T8>, p9:Promise<T9>):Promise<[T1, T2, T3, T4, T5, T6, T7, T8, T9]> {
      return Promise.all<any>( [p1, p2, p3, p4, p5, p6, p7, p8, p9] );
    }

    export function all10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(p1:Promise<T1>, p2:Promise<T2>, p3:Promise<T3>, p4:Promise<T4>, p5:Promise<T5>, p6:Promise<T6>, p7:Promise<T7>, p8:Promise<T8>, p9:Promise<T9>, p10:Promise<T10>):Promise<[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]> {
      return Promise.all<any>( [p1, p2, p3, p4, p5, p6, p7, p8, p9, p10] );
    }

    export function all11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>(p1:Promise<T1>, p2:Promise<T2>, p3:Promise<T3>, p4:Promise<T4>, p5:Promise<T5>, p6:Promise<T6>, p7:Promise<T7>, p8:Promise<T8>, p9:Promise<T9>, p10:Promise<T10>, p11:Promise<T11>):Promise<[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]> {
      return Promise.all<any>( [p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11] );
    }


    
    
    
  }

}