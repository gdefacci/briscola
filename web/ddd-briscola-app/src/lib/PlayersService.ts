import {CurrentPlayer, Player, PlayersCollection, SiteMap, Input} from "ddd-briscola-model"

import {fetch} from "rest-fetch"
import {Http} from "./Util"

export class PlayersService {
	constructor(private siteMap: SiteMap) {
	}
	allPlayers():Promise<Player[]> {
      return fetch(PlayersCollection).from(this.siteMap.players).then( c => c.members )
    }
	player(playerSelf: string):Promise<Player> {
      return fetch(Player).from(playerSelf)
    }
	createPlayer(name: string, password:string):Promise<CurrentPlayer> {
    return Http.POST<Input.Player>(this.siteMap.players, {
      name: name,
      password:password
    }).then( resp => {
        return resp.json().then( pl => fetch(CurrentPlayer).fromObject(pl) )
    })
  }
  logon(name: string, password:string):Promise<CurrentPlayer>  {
    return Http.POST<Input.Player>(this.siteMap.playerLogin, {
      name: name,
      password:password
    }).then( resp => resp.json().then( pl => fetch(CurrentPlayer).fromObject(pl) ) )
  }

}