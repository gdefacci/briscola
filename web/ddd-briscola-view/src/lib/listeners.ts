import {Path, Player,Card, CompetitionState, Board} from "ddd-briscola-model"
import {Commands} from "ddd-briscola-app"

import Command = Commands.Command

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
  onPlayerSelection(player: Player, selected: boolean): void
}

export interface CompetionJoinListener {
  onAcceptCompetition(cid:CompetitionState):void
  onDeclineCompetition(cid:CompetitionState):void
}

export interface CardSelectionListener {
  onSelectedCard(c:Card):void  
}

export interface PlayCardListener {
  onPlayCard(c:Card):void  
}

export interface PlayerDeckSelectionListener {
  onPlayerDeck(gameId:Path, display:boolean):void  
}

export interface GameListener {
  onSelectedGame(gm:Path):void
}

export interface BoardListener extends PlayerLoginListener, StartCompetitionListener, PlayerSelectionListener, CompetionJoinListener, GameListener, PlayCardListener, PlayerDeckSelectionListener {
}

export function createBoardCommandListener(board: Board, commandListener: (cmd: Command) => void) {
  return {
    board:board,
    onStartCompetition: () => {
      commandListener(new Commands.StartCompetition())
    },
    onCreatePlayer: (name: string, password:string) => {
      commandListener(new Commands.CreatePlayer(name, password))
    },
    onPlayerLogin: (name: string, password:string) => {
      commandListener(new Commands.PlayerLogon(name, password))
    },
    onPlayerSelection: (player: Player, selected: boolean) => {
      commandListener(new Commands.SelectPlayerForCompetition(player.self, selected));
    },
    onAcceptCompetition:(cid:CompetitionState) => {
      commandListener(new Commands.AcceptCompetition(cid.self))
    },
    onDeclineCompetition:(cid:CompetitionState) => {
      commandListener(new Commands.DeclineCompetition(cid.self))
    },
    onSelectedGame:(gm:Path) => {
      commandListener(new Commands.SetCurrentGame(gm))
    },
    onPlayCard:(c:Card) => {
      commandListener(new Commands.PlayCard(c))       
    },
    onPlayerDeck:(game:Path, display:boolean) => {
      commandListener(new Commands.DiplayPlayerDeck(game, display))
    }
  }

}
