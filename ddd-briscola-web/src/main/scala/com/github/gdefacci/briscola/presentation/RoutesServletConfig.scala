package com.github.gdefacci.briscola.presentation

import org.obl.raz.Path
import org.obl.raz.TPath
import org.obl.raz.PathPosition

trait RoutesServletConfig {
  
  type SegmentsPath = TPath[PathPosition.Segment, PathPosition.Segment]
  
  def players:SegmentsPath 
  def games:SegmentsPath 
  def competitions:SegmentsPath 
  def siteMap:SegmentsPath 
  def webSocket:SegmentsPath 
  
}

object RoutesServletConfig extends RoutesServletConfig {
  val players = Path / "players"
  val games = Path / "games"
  val competitions = Path / "competitions"
  val siteMap = Path / "site-map"
  val webSocket = Path / "ws"
}