import * as Reducers from "./Reducers"
import { Command } from "./Command"

export default function applicationDispatch(): (cmd: Command) => Reducers.AsynchStateChange {
  return (cmd) => {
    switch (cmd.type) {
      case "startApplication": return Reducers.synchStateChange(st => st)
      case "createPlayer":
      case "playerLogon": return Reducers.playerLogon(cmd)
      case "playCard": return Reducers.playCard(cmd)
      case "acceptCompetition":
      case "declineCompetition":
      case "startCompetition": return Reducers.competitionCommands(cmd)
      case "diplayPlayerDeck": return Reducers.synchReducer(Reducers.diplayPlayerDeck)(cmd)
      case "newDomainEvent": return Reducers.synchReducer(Reducers.newDomainEvent)(cmd)
      case "selectPlayerForCompetition": return Reducers.synchReducer(Reducers.selectPlayerForCompetition)(cmd)
      case "setCompetitionDeadline": return Reducers.synchReducer(Reducers.setCompetitionDeadline)(cmd)
      case "setCompetitionKind": return Reducers.synchReducer(Reducers.setCompetitionKind)(cmd)
      case "setCurrentGame": return Reducers.synchReducer(Reducers.setCurrentGame)(cmd)
      case "updateCompetitionState": return Reducers.synchReducer(Reducers.updateCompetionState)(cmd)
      case "updateGameState": return Reducers.synchReducer(Reducers.updateGameState)(cmd)
      case "updatePlayersState": return Reducers.synchReducer(Reducers.updatePlayersState)(cmd)
    }
  }
}