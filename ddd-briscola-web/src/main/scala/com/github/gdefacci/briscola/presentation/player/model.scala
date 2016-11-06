package com.github.gdefacci.briscola.presentation.player

import org.obl.raz.Path

final case class Player(self:Path, name:String, webSocket:Path, createCompetition:Path)

final case class Team(self:Path, name:String, players:Set[Path])