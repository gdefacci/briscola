enum CommandID {
  startApplication,
  createPlayer, playerLogon,
  startCompetition, acceptCompetition, declineCompetition,
  playCard,
  selectPlayerForCompetition,
  setCompetitionKind,
  setCompetitionDeadline,
  setCurrentGame,
  diplayPlayerDeck,
  updateGameState, updatePlayersState, updateCompetitionState,
  newDomainEvent
}

export default CommandID