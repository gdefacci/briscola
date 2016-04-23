import {JsMap} from "flib"
import {Player} from "ddd-briscola-model"
import {PlayerSelectionListener} from "./listeners"
import cssClasses from "./cssClasses"

/*
import {Card, EmptyCard, CardBack} from "./cards"
import {TabPane, TabPaneItem} from "./TabPane"
import {EventsLog} from "./EventsLog"
import {GameResult} from "./GameResult"
import {PlayerDeckSummary} from "./PlayerDeck"
import {PlayerLogin} from "./PlayerLogin"
*/

export class CurrentPlayer extends React.Component<Player, void> {
  render() {
    var player = this.props;
    return (
      <div className={cssClasses.currentPlayer}>
        <h5>Current player: </h5>
        <h4>{player.name}</h4>
      </div>
    );
  }
}

export interface PlayersProps extends PlayerSelectionListener {
  players: Player[]
  selectedPlayers: JsMap<boolean>
}

export class Players extends React.Component<PlayersProps, void> {
  render() {
    const players = this.props.players;
    const selectedPlayers = this.props.selectedPlayers
    const playersElems = players.map((pl, idx) =>
      <div key={idx}>
        <input type="checkbox"
          onChange={ (e) => this.props.onPlayerSelection(pl, !(selectedPlayers[pl.self] === true)) }
          defaultChecked={selectedPlayers[pl.self]}>
          </input>
        <span>{pl.name}</span>
      </div>
    )
    return (
      <div className={cssClasses.players}>
        {playersElems}
      </div>
    );
  }
}


