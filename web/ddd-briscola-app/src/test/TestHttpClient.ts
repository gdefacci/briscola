import { HttpClient } from "../lib/Http"
import { JsMap } from "flib"

export interface ConfigMap {
  POST?: JsMap<any | ((body: any) => any)>
  PUT?: JsMap<any | ((body: any) => any)>
  DELETE?: JsMap<any | ((body: any) => any)>
}

function serveFromMap(mp: null | undefined | JsMap<any | ((body: any) => any)>, url: string, body: any) {
  if (mp && mp[url]) {
    const r = mp[url]
    const respBdy0 = (typeof r === "function") ? r(body) : r;
    const respBdy = (typeof respBdy0 === "string") ? respBdy0 : JSON.stringify(respBdy0, null, 2)
    return Promise.resolve(new Response(respBdy))
  } else {
    return Promise.resolve(new Response("Not found " + url, {
      status: 404
    }))
  }
}

export class TestClient implements HttpClient {

  constructor(private configMap: ConfigMap = {}) {
  }

  POST<B>(url: string, body?: B): Promise<Response> {
    return serveFromMap(this.configMap.POST, url, body);
  }
  PUT<B>(url: string, body?: B): Promise<Response> {
    return serveFromMap(this.configMap.PUT, url, body);
  }
  DELETE(url: string): Promise<Response> {
    return serveFromMap(this.configMap.DELETE, url, null);
  }

}