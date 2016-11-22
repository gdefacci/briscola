/*

Actually this module is never used, command names are inlined as strin literal, to take advantage of typescript adts.

*/

module CommandID {
  export const startApplication = "command.startApplication"
  export const createPlayer = "command.createPlayer"
  export const playerLogon = "command.playerLogon"
  export const startCompetition = "command.startCompetition"
  export const acceptCompetition = "command.acceptCompetition"
  export const declineCompetition = "command.declineCompetition"
  export const playCard = "command.playCard"
  export const selectPlayerForCompetition = "command.selectPlayerForCompetition"
  export const setCompetitionKind = "command.setCompetitionKind"
  export const setCompetitionDeadline = "command.setCompetitionDeadline"
  export const setCurrentGame = "command.setCurrentGame"
  export const diplayPlayerDeck = "command.diplayPlayerDeck"
  export const updateGameState = "command.updateGameState"
  export const updatePlayersState = "command.updatePlayersState"
  export const updateCompetitionState = "command.updateCompetitionState"
  export const newDomainEvent = "command.newDomainEvent"
}

export default CommandID