import * as Model from "ddd-briscola-model"

import {CompetionJoinListener, GameListener} from "./listeners"

import PlayerEventKind = Model.PlayerEventKind
import BriscolaEventKind = Model.BriscolaEventKind
import CompetitionEventKind = Model.CompetitionEventKind
import cssClasses from "./cssClasses"

export type EventLogEvent = Model.BriscolaEvent | Model.CompetitionEvent | Model.PlayerEvent

export interface EventLogProps extends CompetionJoinListener, GameListener {
  events: EventLogEvent[]
}

export class EventsLog extends React.Component<EventLogProps, void> {
  render() {
    const props = this.props
    const events = props.events
    const elemEvs = events.map((event, i) => {
      switch (event.eventName) {
        case PlayerEventKind[PlayerEventKind.playerLogOn]: {
          const ev = event as Model.PlayerLogOn
          return (
            <div key={i}>Player <b>{ev.player.name}</b> log on</div>
          )
        }

        case PlayerEventKind[PlayerEventKind.playerLogOff]: {
          const ev = event as Model.PlayerLogOff
          return (
            <div key={i}>Player <b>{ev.player.name}</b> log off</div>
          )
        }

        case CompetitionEventKind[CompetitionEventKind.createdCompetition]: {
          const ev = event as Model.CreatedCompetition
          return (
            <div key={i}>
              <p>Player {ev.issuer.name} invited you !!!</p>
              <p>players are {ev.competition.competition.players.map(pl => <b>{pl.name} </b>) } of kind <b>{Model.MatchKindKind[ev.competition.competition.kind.kind]}</b> </p>
              <p>
              {ev.competition.accept.map(url =>
                <button onClick={ clkEv => props.onAcceptCompetition(ev.competition) } key="1" >Accept competiton</button>
              ).getOrElse( () => <noscript /> )}
              {ev.competition.decline.map(url =>
                <button onClick={ clkEv => props.onDeclineCompetition(ev.competition) } key="1" >Decline competiton</button>
              ).getOrElse( () => <noscript /> )}
              </p>
            </div>
          )
        }

        case BriscolaEventKind[BriscolaEventKind.gameStarted]: {
          const ev = event as Model.GameStarted
          return (
            <div key={i}>
              <a href="#" onClick={ cev => props.onSelectedGame(ev.game.self) } >A game has started</a>
            </div>
          )
        }

        case BriscolaEventKind[BriscolaEventKind.cardPlayed]: {
          const ev = event as Model.CardPlayed
          return (
            <div key={i}>
              <a href="#" onClick={ cev => props.onSelectedGame(ev.game.self) } >{ev.player.name} played {ev.card.number} {Model.Seed[ev.card.seed]}</a>
            </div>
          )
        }

        default: {
          return (
            <div key={i}>
              <textarea rows={4} cols={80} defaultValue={JSON.stringify(event)}></textarea>
            </div>
          )
        }
      }
    })

    return (
      <div className={cssClasses.eventLog}>
        {elemEvs}
      </div>
    );
  }
}
