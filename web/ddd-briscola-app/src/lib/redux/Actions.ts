import {Card, Board, Path, Input} from "ddd-briscola-model"

const IDs = {
  CreatePlayer:"CreatePlayer",
  PlayerLogon:"PlayerLogon",
  StartCompetition:"StartCompetition",
  AcceptCompetition:"AcceptCompetition",
  DeclineCompetition:"DeclineCompetition",
  PlayCard:"PlayCard",
  SelectPlayerForCompetition:"SelectPlayerForCompetition",
  SetCompetitionKind:"SetCompetitionKind",
  SetCompetitionDeadline:"SetCompetitionDeadline",
  SetCurrentGame:"SetCurrentGame",
  DiplayPlayerDeck:"DiplayPlayerDeck"
}

export module Actions {

  export function createPlayerAction(a:{ type:string }) {

  }

  export function createPlayer(playerName: string, password:string) {
    return {
      type:IDs.CreatePlayer,
      playerName,
      password
    }
  }

  export function playerLogon(playerName: string, password:string) {
    return {
      type:IDs.PlayerLogon,
      playerName,
      password
    }
  }

  export function startCompetition() {
    return {
      type:IDs.StartCompetition
    }
  }

  export function acceptCompetition(competition: Path) {
    return {
      type:IDs.AcceptCompetition,
      competition
    }
  }

  export function declineCompetition(competition: Path) {
    return {
      type:IDs.DeclineCompetition,
      competition
    }
  }

  export function playCard(card:Card) {
    return {
      type:IDs.PlayCard,
      card
    }
  }

  export function selectPlayerForCompetition(player: Path, selected:boolean) {
    return {
      type:IDs.SelectPlayerForCompetition,
      player,
      selected
    }
  }

  export function setCompetitionKind(kind: Input.MatchKind) {
     return {
      type:IDs.SetCompetitionKind,
      kind
    }
  }

  export function setCompetitionDeadline(deadlineKind: Input.CompetitionStartDeadline) {
    return {
      type:IDs.SetCompetitionDeadline,
      deadlineKind
    }
  }

  export function setCurrentGame(game: Path) {
    return {
      type:IDs.SetCurrentGame,
      game
    }
  }

  export function diplayPlayerDeck(game:Path, display:boolean) {
    return {
      type:IDs.DiplayPlayerDeck,
      game,
      display
    }
  }

}