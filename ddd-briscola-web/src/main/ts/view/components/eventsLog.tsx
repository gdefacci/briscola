/// <reference path='../../_all.ts' />

module View.Components {

  import PlayerEventKind = Model.PlayerEventKind
  import BriscolaEventKind = Model.BriscolaEventKind
  import CompetitionEventKind = Model.CompetitionEventKind

  type EventLogEvent = Model.BriscolaEvent | Model.CompetitionEvent | Model.PlayerEvent

  interface EventLogProps extends CompetionJoinListener, GameListener {
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
                Player {ev.issuer.name} invited you !!!players {ev.competition.competition.players.map(pl => <b>{pl.name}</b>) } kind <b>{ev.competition.competition.kind}</b>
                {ev.competition.accept.map(url =>
                  <button
                    onClick={ clkEv => props.onAcceptCompetition(ev.competition) } >Accept competiton</button>)
                }
                {ev.competition.decline.map(url =>
                  <button
                    onClick={ clkEv => props.onDeclineCompetition(ev.competition) }>Decline competiton</button>)
                }
              </div>
            )
          }

          case BriscolaEventKind[BriscolaEventKind.gameStarted]: {
            const ev = event as Model.GameStarted
            return (
              <div key={i}>
                A game has started <a href="#" onClick={ cev => props.onSelectedGame(ev.game.self) } />
              </div>
            )
          }

          default: {
            return (
              <div key={i}>
                <textarea rows={4} cols={80}>{JSON.stringify(event) }</textarea>
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
}