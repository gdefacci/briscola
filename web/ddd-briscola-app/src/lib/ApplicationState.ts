import { Option } from "flib"
import { PlayersService } from "./PlayersService"
import { PlayerService } from "./PlayerService"
import { ResourceFetch, ExtraPropertiesStrategy, httpCacheFactory, mapping } from "nrest-fetch"
import { Board, SiteMap, CurrentPlayer } from "ddd-briscola-model"
import { Http } from "./Util"

export interface ApplicationState {
  playersService: PlayersService
  playerService: Option<PlayerService>
  createPlayerService: (player: CurrentPlayer) => PlayerService
  board: Board
}

export function initialState(entryPoint: string): Promise<ApplicationState> {
  const reqFactory = Http.createRequestFactory({
    method: "GET"
  })
  const resourceFetch = new ResourceFetch(ExtraPropertiesStrategy.copy, httpCacheFactory(reqFactory, Http.jsonResponseReader))
  const createPlayerService = (player: CurrentPlayer) => new PlayerService(resourceFetch, player)

  return resourceFetch.fetchResource(entryPoint, mapping(SiteMap)).then(siteMap => {
    return {
      playersService: new PlayersService(resourceFetch, siteMap),
      playerService: Option.None,
      board: Board.empty(),
      createPlayerService
    }
  })
}