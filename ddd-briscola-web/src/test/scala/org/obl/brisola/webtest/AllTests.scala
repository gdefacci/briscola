package org.obl.brisola.webtest

import org.scalatest.Sequential

 class AllTests extends Sequential(
   new GivenOnePlayer,
   new Given2Players
 )