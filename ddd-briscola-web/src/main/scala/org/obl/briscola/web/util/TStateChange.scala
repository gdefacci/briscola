package org.obl.briscola.web.util

case class TStateChange[SA,E,SB](oldState:SA, event:E, newState:SB) 