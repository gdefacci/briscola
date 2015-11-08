import {Option} from "flib"
import * as Commands from "./Commands"
import {PlayersService} from "./PlayersService"
import {PlayerService} from "./PlayerService"
import {Board, SiteMap, GameState, ActiveGameState, FinalGameState, Player, CurrentPlayer, CompetitionState, ViewFlag} from "ddd-briscola-model"
import {fetch} from "rest-fetch"

import Command = Commands.Command

export interface App {
  displayChannel: Rx.Observable<Board>
  exec(cmd: Command):void
}

export module App {
  export function create(entryPoint:string):App {
    return new AppImpl(entryPoint)
  }
}

class AppImpl implements App {
  playersService: Promise<PlayersService>
  playerService: Option<Promise<PlayerService>> = Option.none<Promise<PlayerService>>()
  displayChannel = new Rx.ReplaySubject<Board>()
  commandChannel = new Rx.Subject<Command>()
  state: Board

  constructor(entryPoint: string) {
    this.playersService = fetch(SiteMap).from(entryPoint).then(siteMap => new PlayersService(siteMap))
    this.state = Board.empty()

    const self = this;
    this.commandChannel.subscribe({
      onNext(cmd: Command): void {
        self.process(cmd)
      },
      onError(exception: any): void {
        console.log("error")
        console.log(exception)
        if (exception["stack"]) {
          console.log(exception["stack"])
        }
      },
      onCompleted(): void {
        console.log("completed?")
      }
    })

    this.displayChannel.onNext(this.state)
  }

  withPlayerService<T>(f: (ps: PlayerService) => T): Option<Promise<T>> {
    return this.playerService.fold(
      () => Option.none<Promise<T>>(),
      ps => Option.some(ps.then(f))
    )
  }

  invalidCommand(cmd: Command): Promise<any> {
    console.error("invalid command")
    console.error(cmd)
    return Promise.reject(new Error("invalid Command " + cmd))
  }

  onError(exception: any) {
    console.log("error")
    console.log(exception)
    if (exception instanceof Error) {
      console.log(exception.stack)
    }
  }

  exec(cmd: Command):void {
    this.commandChannel.onNext(cmd)
  }

  updateGameState(gm: GameState):void {
    if (gm instanceof ActiveGameState) {
      this.state.activeGames[gm.self] = gm;
    } else if (gm instanceof FinalGameState) {
      delete this.state.activeGames[gm.self];
      this.state.finishedGames[gm.self] = gm;
    }
    this.state.currentGame.fold<void>(
      () => this.state.currentGame = Option.some(gm),
      (cgm) => {
        if (gm.self === cgm.self) {
          this.state.currentGame = Option.some(gm)
        }
      })
  }

  updateCompetitionState(comp: CompetitionState):void {
    this.state.engagedCompetitions[comp.self] = comp;
  }

  updatePlayerState(pl: Player):void {
    this.state.player = Option.some(pl)
  }

  updatePlayersState(plyrs: Player[]):void {
    const nps = this.state.player.map( cp => plyrs.filter( pl => pl.self !== cp.self)).getOrElse( () => plyrs)
    this.state.players = nps;
  }

  private viewRefresh() {
    this.exec(new Commands.DisplayBoardCommand(this.state))
  }

  listenPlayerEvents(ps: PlayerService) {
    ps.eventsLog.subscribe(event => {
      this.state.eventsLog.unshift(event)
      this.viewRefresh()
    })

    ps.gamesChannel.subscribe(es => {
      const gm: GameState = es.game
      this.updateGameState(gm)
      this.viewRefresh()
    })

    ps.competitionsChannel.subscribe(es => {
      const comp: CompetitionState = es.competition
      this.updateCompetitionState(comp)
      this.viewRefresh()
    })

    ps.playersChannel.subscribe(es => {
      const players: Player[] = es.players
      this.updatePlayersState(players)
      this.viewRefresh()
    })
  }

  private createPlayerService(playerPromise:Promise<CurrentPlayer>):Promise<PlayerService> {
    return playerPromise.then(player => {
      const r = new PlayerService(player)
      this.listenPlayerEvents(r)
      this.updatePlayerState(r.player)
      return r;
    })
  }

  process(cmd: Command): Promise<any> {
    if (cmd instanceof Commands.DisplayBoardCommand) {
      return Promise.resolve(this.displayChannel.onNext(cmd.board))
    } else if (cmd instanceof Commands.DisplayCommand) {
      return this.processDisplayCommand(cmd)
    } else if (cmd instanceof Commands.PlayerLogon) {
      const createPlayer = this.playersService.then(ps => ps.logon(cmd.playerName, cmd.password))
      const psp = this.createPlayerService(createPlayer)
      this.playerService = Option.some(psp)
      return psp.then( p => this.viewRefresh() )
    } else if (cmd instanceof Commands.CreatePlayer) {
      const createPlayer = this.playersService.then(ps => ps.createPlayer(cmd.playerName, cmd.password))
      const psp = this.createPlayerService(createPlayer)
      this.playerService = Option.some(psp)
      return psp.then( p => this.viewRefresh() )
    } else if (cmd instanceof Commands.GameCommand) {
      return this.processGameCommand(cmd);
    } else if (cmd instanceof Commands.CompetitionCommand) {
      return this.processCompetitionCommand(cmd);
    }
  }

  processDisplayCommand(cmd: Commands.DisplayCommand): Promise<any> {
    const fullfill = ()  => {
      this.displayChannel.onNext(this.state)
      return Promise.resolve(null)
    }

    if (cmd instanceof Commands.SelectPlayerForCompetition) {
      if (cmd.selected) {
        this.state.competitionSelectedPlayers[cmd.player] = true;
      } else {
        delete this.state.competitionSelectedPlayers[cmd.player];
      }
      return fullfill();
    } else if (cmd instanceof Commands.SetCompetitionKind) {
      this.state.competitionKind = cmd.kind;
      return fullfill();
    } else if (cmd instanceof Commands.SetCompetitionDeadline) {
      this.state.competitionDeadlineKind = cmd.deadlineKind;
      return fullfill();
    } else if (cmd instanceof Commands.SetCurrentGame) {
      this.state.currentGame = Option.option<GameState>(this.state.activeGames[cmd.game]).orElse( () => Option.option(this.state.finishedGames[cmd.game]));
      return fullfill();
    } else if (cmd instanceof Commands.DiplayPlayerDeck) {
      this.state.viewFlag = cmd.display === true ? ViewFlag.showPlayerCards : ViewFlag.normal;
      return fullfill();
    } else {
      return this.invalidCommand(cmd);
    }
  }

  processCompetitionCommand(cmd: Commands.CompetitionCommand): Promise<any> {
    return this.withPlayerService(playerService => {
      if (cmd instanceof Commands.StartCompetition) {
        return playerService.createCompetition(Object.keys(this.state.competitionSelectedPlayers), this.state.competitionKind, this.state.competitionDeadlineKind)
      } else if (cmd instanceof Commands.AcceptCompetition) {
        return playerService.acceptCompetition(cmd.competition)
      } else if (cmd instanceof Commands.DeclineCompetition) {
        return playerService.declineCompetition(cmd.competition)
      } else {
        return this.invalidCommand(cmd);
      }
    }).getOrElse(() => Promise.reject(this.onError(new Error("empty player service"))))
  }

  processGameCommand(cmd: Commands.GameCommand): Promise<any> {
    return this.withPlayerService(playerService => {
      if (cmd instanceof Commands.PlayCard) {
        const game = this.state.currentGame.map(g => Promise.resolve(g.self)).getOrElse(() => Promise.reject("no current game"))
        return game.then(game => playerService.playCard(game, cmd.card))
      } else {
        return this.invalidCommand(cmd);
      }
    }).getOrElse(() => Promise.reject(this.onError(new Error("empty player service"))))
  }
}

