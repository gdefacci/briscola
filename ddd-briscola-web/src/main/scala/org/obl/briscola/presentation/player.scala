package org.obl.briscola.presentation

import org.obl.raz.Path

final case class Player(self:Path, name:String, webSocket:Path, createCompetition:Path)

final case class Team(self:Path, name:String, players:Set[Path])