package com.github.gdefacci.briscola

import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.ddd.StateChange

package object presentation {
  
    type StateChangeFilter[S, E, PS, PE] = PlayerId => PartialFunction[StateChange[S, E], EventAndState[PE, PS]]

}