module View {

  export interface Clickable {
    onClick?():void  
  }
  
  export interface StartCompetitionListener {
    onStartCompetition(): void
  }

  export interface PlayerLoginListener {
    onCreatePlayer(name: string, password: string): void
    onPlayerLogin(name: string, password: string): void
  }

  export interface PlayerSelectionListener {
    onPlayerSelection(player: Model.Player, selected: boolean): void
  }
  
  export interface CompetionJoinListener {
    onAcceptCompetition(cid:Model.CompetitionState):void
    onDeclineCompetition(cid:Model.CompetitionState):void
  }
  
  export interface CardSelectionListener {
    onSelectedCard(c:Model.Card):void  
  }
  
  export interface PlayCardListener {
    onPlayCard(c:Model.Card):void  
  }
  
  export interface PlayerDeckSelectionListener {
    onPlayerDeck(dck:Model.Card[]):void  
  }
  
  export interface GameListener {
    onSelectedGame(gm:Model.Ws.Path):void
  }

  export interface BoardListener extends PlayerLoginListener, StartCompetitionListener, PlayerSelectionListener, CompetionJoinListener, GameListener, PlayCardListener, PlayerDeckSelectionListener {
  }

  export function createBoardCommandListener(board: Model.Board, commandListener: (cmd: Model.Command) => void) {
    return {
      board:board,
      onStartCompetition: () => {
        commandListener(new Model.Commands.StartCompetition())
      },
      onCreatePlayer: (name: string, password:string) => {
        commandListener(new Model.Commands.CreatePlayer(name, password))
      },
      onPlayerLogin: (name: string, password:string) => {
        commandListener(new Model.Commands.PlayerLogon(name, password))
      },
      onPlayerSelection: (player: Model.Player, selected: boolean) => {
        commandListener(selected ? new Model.Commands.SelectPlayerForCompetition(player.self) : new Model.Commands.UnselectPlayerForCompetition(player.self));
      },
      onAcceptCompetition:(cid:Model.CompetitionState) => {
        commandListener(new Model.Commands.AcceptCompetition(cid.self))
      },
      onDeclineCompetition:(cid:Model.CompetitionState) => {
        commandListener(new Model.Commands.DeclineCompetition(cid.self))
      },
      onSelectedGame:(gm:Model.Ws.Path) => {
        commandListener(new Model.Commands.SetCurrentGame(gm))
      },
      onPlayCard:(c:Model.Card) => {
        commandListener(new Model.Commands.PlayCard(c))       
      },
      onPlayerDeck:(dck:Model.Card[]) => {
        
      }
    }

  }
}