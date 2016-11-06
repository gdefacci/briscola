package com.github.gdefacci

import scalaz.{ -\/, \/, \/- }

package object ddd {

  type CommandRunner[S, C, E, Err] = (S, C) => Err \/ (Seq[E], S)
  type StateChangeCommandRunner[S, C, E, Err] = (S, C) => Err \/ Seq[StateChange[S, E]]
  
  type CommandsRunner[S, C, E, Err] = (S, Seq[C]) => Err \/ (Seq[E], S)
  type StateChangeCommandsRunner[S, C, E, Err] = (S, Seq[C]) => Err \/ Seq[StateChange[S, E]]

  object Runner {

    def apply[S, C, E, Err](decider: Decider[S, C, E, Err], evolver: Evolver[S, E]): CommandRunner[S, C, E, Err] = { (state, cmd) =>
      decider(state, cmd).map { evs =>
        evs -> evolver(state, evs)
      }
    }

    def fromCommandSeq[S, C, E, Err](decider: Decider[S, C, E, Err], evolver: Evolver[S, E]): CommandsRunner[S, C, E, Err] = {
      val runner = apply[S, C, E, Err](decider, evolver)
      (state, commands) => {
        val z: Err \/ (Seq[E], S) = \/-(Nil, state)
        commands.foldLeft(z) { (acc, cmd) =>
          acc.flatMap { p =>
            val (events, state) = p
            runner(state, cmd).map(e1 => (events ++ e1._1) -> e1._2)
          }
        }
      }
    }

    def changesFromCommandSeq[S, C, E, Err](decider: Decider[S, C, E, Err], evolver: Evolver[S, E]): StateChangeCommandsRunner[S, C, E, Err] = {
      val runner = changes[S, C, E, Err](decider, evolver)
      (state, commands) => {
        val z: Err \/ (Seq[StateChange[S, E]], S) = \/-(Nil, state)
        commands.foldLeft(z) { (acc, cmd) =>
          acc.flatMap { p =>
            val (changes, state) = p
            runner(state, cmd).map(sc => (changes ++ sc) -> sc.last.state)
          }
        }.map(_._1)
      }
    }

    def changes[S, C, E, Err](decider: Decider[S, C, E, Err], evolver: Evolver[S, E]): StateChangeCommandRunner[S, C, E, Err] = { (state, cmd) =>
      decider(state, cmd).map { evs =>
        evolver.changes(state, evs)
      }
    }

  }
}