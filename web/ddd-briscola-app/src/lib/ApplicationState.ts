import { Option } from "flib"
import { PlayersService } from "./PlayersService"
import { PlayerService } from "./PlayerService"
import { ResourceFetch, ExtraPropertiesStrategy, httpCacheFactory, mapping } from "nrest-fetch"
import { Board, SiteMap, CurrentPlayer } from "ddd-briscola-model"
import { HttpConfig, HttpClient, browserHttpClient } from "./Http"
import { observableWebSocket } from "./Util"
import {Observable} from '@reactivex/rxjs';

export interface ApplicationState {
  playersService: PlayersService
  playerService: Option<PlayerService>
  createPlayerService: (player: CurrentPlayer) => PlayerService
  board: Board
}

export function initialState(entryPoint: string): Promise<ApplicationState> {
  const reqFactory = HttpConfig.createRequestFactory({
    method: "GET"
  })
  const resourceFetch = new ResourceFetch(ExtraPropertiesStrategy.copy, httpCacheFactory(reqFactory, HttpConfig.jsonResponseReader))
  const webSocketFactory = (url:string) => observableWebSocket(url)
  return createInitialState(resourceFetch, browserHttpClient, webSocketFactory, entryPoint)
}

export function createInitialState(
  resourceFetch:ResourceFetch,
  httpClient:HttpClient,
  webSocketFactory:(url: string) => Observable<MessageEvent>,
  entryPoint: string):Promise<ApplicationState> {

  const createPlayerService = (player: CurrentPlayer) => new PlayerService(resourceFetch, httpClient, webSocketFactory, player)

  return resourceFetch.fetchResource(entryPoint, mapping(SiteMap)).then(siteMap => {
    return {
      playersService: new PlayersService(resourceFetch, httpClient, siteMap),
      playerService: Option.None,
      board: Board.empty(),
      createPlayerService
    }
  })
}