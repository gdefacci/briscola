package org.obl.brisola.webtest

import org.scalatest.Sequential

 class AllTests1 extends Sequential(
   new GivenNoPlayers,
   new GivenOnePlayer,
   new Given2Players
 )