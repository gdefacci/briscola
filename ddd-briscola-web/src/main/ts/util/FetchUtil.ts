module Util {

  type Path = string

  export type Retrieve<I, T> = (p: I) => Promise<T>
  export type Fetch<T> = Retrieve<Path, T>

  export module Retrieve {

    export function opt<I, T>(r: Retrieve<I, T>):Retrieve<I, Std.Option<T>> {
      return (i:I) => {
        if (Std.isNull(i)) return Promise.resolve(Std.none<T>());
        else return r(i).then(r => Std.some(r))
      }
    }
  }

  export function fetch<T>(url: string, init?: RequestInit): Promise<T> {
    return window.fetch(url, init).then(resp => {
      if (resp.status >= 200 && resp.status < 300) {
        return resp.text()
      } else {
        const mthd = init === undefined ? "GET" : init.method
        return resp.text().then( txt => Promise.reject(`${mthd} ${url} : (${resp.status}) ${txt}`), err => Promise.reject(`${mthd} ${url} : (${resp.status}) ${err}`))  
      }
    }).then(txt => {
      try {
        return Promise.resolve(<T>JSON.parse(txt))
      } catch(e) {
        return Promise.reject(`error parsing content at ${url}, content:\n${txt}`)
      }
    
    })
  }

  export module Fetch {
    
    export function GET<T>(url:string):Promise<T> {
      return fetch(url, {
        method:"GET"
      }) 
    }
    
    function fetchWithBody<B,T>(mthd:string):(url:string, body?:B)  =>  Promise<T> {
      return (url, body) => {
        const bdy = !Std.isNull(body) ? JSON.stringify(body) : undefined
        return fetch(url, {
          method:mthd,
          body:bdy
        })   
      }
    }
    
    export function POST<B,T>(url:string, body?:B):Promise<T> {
      return fetchWithBody<B, T>("POST")(url, body)
    }   
    
    export function PUT<B,T>(url:string, body?:B):Promise<T> {
      return fetchWithBody<B, T>("PUT")(url, body)
    }   
    
    export function DELETE<T>(url:string):Promise<T> {
      return fetch(url, {
        method:"DELETE"
      }) 
    }
  }
  
}