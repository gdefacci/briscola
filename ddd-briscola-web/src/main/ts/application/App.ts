namespace Application {

  import Option = Std.Option
  import Commands = Model.Commands

  export interface App {
    displayChannel: Rx.Observable<Model.Board>
    exec(cmd: Model.Command):void
  }
  
  export module App {
    export function create(entryPoint:string):App {
      return new AppImpl(entryPoint)    
    }  
  }

  class AppImpl implements App {
    playersService: Promise<PlayersService>
    playerService: Std.Option<Promise<PlayerService>> = Std.none<Promise<PlayerService>>()
    displayChannel = new Rx.ReplaySubject<Model.Board>()
    commandChannel = new Rx.Subject<Model.Command>()
    state: Model.Board

    constructor(entryPoint: string) {
      this.playersService = ToModel.siteMapFetch(entryPoint).then(siteMap => new PlayersService(siteMap))
      this.state = Model.Board.empty()

      const self = this;
      this.commandChannel.subscribe({
        onNext(cmd: Model.Command): void {
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
        () => Std.none<Promise<T>>(),
        ps => Std.some(ps.then(f))
      )
    }

    invalidCommand(cmd: Model.Command): Promise<any> {
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

    exec(cmd: Model.Command):void {
      this.commandChannel.onNext(cmd)
    }

    updateGameState(gm: Model.GameState):void {
      if (gm instanceof Model.ActiveGameState) {
        this.state.activaGames[gm.self] = gm;
      } else if (gm instanceof Model.FinalGameState) {
        delete this.state.activaGames[gm.self];
        this.state.finishedGames[gm.self] = gm;
      }
      this.state.currentGame.fold<void>(
        () => this.state.currentGame = Std.some(gm),
        (cgm) => {
          if (gm.self === cgm.self) {
            this.state.currentGame = Std.some(gm)  
          }
        })
    }
    
    updateCompetitionState(comp: Model.CompetitionState):void {
      this.state.engagedCompetitions[comp.self] = comp;
    }
    
    updatePlayerState(pl: Model.Ws.Player):void {
      this.state.player = Std.some(pl)
    }
    
    updatePlayersState(plyrs: Model.Player[]):void {
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
        const gm: Model.GameState = es.state
        this.updateGameState(gm)
        this.viewRefresh()
      })
      
      ps.competitionsChannel.subscribe(es => {
        const comp: Model.CompetitionState = es.state
        this.updateCompetitionState(comp)
        this.viewRefresh()
      })
      
      ps.playersChannel.subscribe(es => {
        const players: Model.Player[] = es.state
        this.updatePlayersState(players)
        this.viewRefresh()
      })
    }
    
    private createPlayerService(playerPromise:Promise<Model.Player>):Promise<PlayerService> {
      return playerPromise.then(player => {
        const r = new PlayerService(new PlayerWsService(player))
        this.listenPlayerEvents(r)
        this.updatePlayerState(r.player)
        return r;
      })  
    }

    process(cmd: Model.Command): Promise<any> {
      if (cmd instanceof Commands.DisplayBoardCommand) {
        return Promise.resolve(this.displayChannel.onNext(cmd.board))
      } else if (cmd instanceof Model.DisplayCommand) {
        return this.processDisplayCommand(cmd)
      } else if (cmd instanceof Commands.PlayerLogon) {
        const createPlayer = this.playersService.then(ps => ps.logon(cmd.playerName, cmd.password))
        const psp = this.createPlayerService(createPlayer)
        this.playerService = Std.some(psp)
        return psp.then( p => this.viewRefresh() )
      } else if (cmd instanceof Commands.CreatePlayer) {
        const createPlayer = this.playersService.then(ps => ps.createPlayer(cmd.playerName, cmd.password))
        const psp = this.createPlayerService(createPlayer)
        this.playerService = Std.some(psp)
        return psp.then( p => this.viewRefresh() )
      } else if (cmd instanceof Model.GameCommand) {
        return this.processGameCommand(cmd);
      } else if (cmd instanceof Model.CompetitionCommand) {
        return this.processCompetitionCommand(cmd);
      }
    }

    processDisplayCommand(cmd: Model.DisplayCommand): Promise<any> {
      const fullfill = ()  => {
        this.displayChannel.onNext(this.state)
        return Promise.resolve(null)  
      }
      
      if (cmd instanceof Commands.SelectPlayerForCompetition) {
        this.state.competitionSelectedPlayers[cmd.player] = true;
        return fullfill();
      } else if (cmd instanceof Commands.UnselectPlayerForCompetition) {
        delete this.state.competitionSelectedPlayers[cmd.player];
        return fullfill();
      } else if (cmd instanceof Commands.SetCompetitionKind) {
        this.state.competitionKind = cmd.kind;
      } else if (cmd instanceof Commands.SetCompetitionDeadline) {
        this.state.competitionDeadlineKind = cmd.deadlineKind;
      } else if (cmd instanceof Commands.SetCurrentGame) {
        this.state.currentGame = Std.option<Model.GameState>(this.state.activaGames[cmd.game]).orElse( () => Std.option(this.state.finishedGames[cmd.game]));
      } else {
        return this.invalidCommand(cmd);
      }
    }
    
    processCompetitionCommand(cmd: Model.CompetitionCommand): Promise<any> {
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

    processGameCommand(cmd: Model.GameCommand): Promise<any> {
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

}