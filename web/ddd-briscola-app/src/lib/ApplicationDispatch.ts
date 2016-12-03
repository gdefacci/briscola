import * as Reducers from "./Reducers"
import { synchReducer,synchStateChange } from "./Red"
import { Command } from "./Command"

export default function applicationDispatch(): (cmd: Command) => Reducers.AsynchStateChange {
  return (cmd) => {
    switch (cmd.type) {
      case "startApplication": return synchStateChange(st => st)
      case "createPlayer":
      case "playerLogon": return Reducers.playerLogon(cmd)
      case "playCard": return Reducers.playCard(cmd)
      case "acceptCompetition":
      case "declineCompetition":
      case "startCompetition": return Reducers.competitionCommands(cmd)
      case "diplayPlayerDeck": return synchReducer(Reducers.diplayPlayerDeck)(cmd)
      case "newDomainEvent": return synchReducer(Reducers.newDomainEvent)(cmd)
      case "selectPlayerForCompetition": return synchReducer(Reducers.selectPlayerForCompetition)(cmd)
      case "setCompetitionDeadline": return synchReducer(Reducers.setCompetitionDeadline)(cmd)
      case "setCompetitionKind": return synchReducer(Reducers.setCompetitionKind)(cmd)
      case "setCurrentGame": return synchReducer(Reducers.setCurrentGame)(cmd)
      case "updateCompetitionState": return synchReducer(Reducers.updateCompetionState)(cmd)
      case "updateGameState": return synchReducer(Reducers.updateGameState)(cmd)
      case "updatePlayersState": return synchReducer(Reducers.updatePlayersState)(cmd)
    }
  }
}