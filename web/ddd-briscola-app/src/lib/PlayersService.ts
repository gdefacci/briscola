import {CurrentPlayer, Player, PlayersCollection, SiteMap, Input} from "ddd-briscola-model"

import {ResourceFetch, mapping} from "nrest-fetch"
import {Http} from "./Util"

export class PlayersService {
	constructor(private resourceFetch:ResourceFetch, private siteMap: SiteMap) {
	}
	allPlayers():Promise<Player[]> {
      return this.resourceFetch.fetchResource(this.siteMap.players, mapping(PlayersCollection)).then( c => c.members )
    }
	player(playerSelf: string):Promise<Player> {
      return this.resourceFetch.fetchResource(playerSelf, mapping(Player))
    }
	createPlayer(name: string, password:string):Promise<CurrentPlayer> {
    return Http.POST<Input.Player>(this.siteMap.players, {
      name: name,
      password:password
    }).then( resp => {
        return resp.json().then( pl => this.resourceFetch.fetchObject(pl, mapping(CurrentPlayer)) )
    })
  }
  logon(name: string, password:string):Promise<CurrentPlayer>  {
    return Http.POST<Input.Player>(this.siteMap.playerLogin, {
      name: name,
      password:password
    }).then( resp => resp.json().then( pl => this.resourceFetch.fetchObject(pl, mapping(CurrentPlayer)) ) )
  }

}