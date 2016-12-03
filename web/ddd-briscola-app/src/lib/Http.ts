export interface HttpClient {
  POST<B>(url: string, body?: B): Promise<Response>
  PUT<B>(url: string, body?: B): Promise<Response>
  DELETE(url: string): Promise<Response>
}

function fetchWithBody<B>(mthd: string, url: string, body?: B):Promise<Response> {
  if (url === undefined || url === null) throw new Error("making request at null url, body : " + body)

  const bdy = (body !== undefined && body !== null) ? JSON.stringify(body) : undefined
  return window.fetch(url, {
    method: mthd,
    body: bdy
  })
}

export const browserHttpClient: HttpClient = {

  POST<B>(url: string, body?: B): Promise<Response> {
    return fetchWithBody<B>("POST",url, body)
  },
  PUT<B>(url: string, body?: B): Promise<Response> {
    return fetchWithBody<B>("PUT", url, body)
  },
  DELETE(url: string): Promise<Response> {
    return fetchWithBody("DELETE", url)
  }

}

export module HttpConfig {

  function parseJson(txt: string): Promise<any> {
    try {
      return Promise.resolve(JSON.parse(txt))
    } catch (e) {
      return Promise.reject(`Error parsing json ${e.message}`)
    }
  }

  function statusOk(resp: Response): boolean {
    const st = resp.status
    return (st >= 200 && st < 300);
  }

  function reqDesc(req: Request): string {
    return `${req.method} ${req.url}`
  }

  export const jsonResponseReader = (resp: Response, req: Request): Promise<any> => {
    if (statusOk(resp)) {
      return resp.text().then(parseJson)
    } else {
      return Promise.reject(`${reqDesc(req)} return status : ${resp.status}`)
    }
  }

  export function createRequestFactory(reqInit: RequestInit): (url: string) => Request {
    return (url: string) => {
      return new Request(url, reqInit);
    }
  }

}
