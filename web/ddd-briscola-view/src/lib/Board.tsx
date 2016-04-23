import {isNull} from "flib"
import {GameState, ActiveGameState} from "ddd-briscola-model"
import * as Model from "ddd-briscola-model"
import {GameListener, BoardListener, StartCompetitionListener, PlayerDeckSelectionListener, PlayCardListener} from "./listeners"
import cssClasses from "./cssClasses"
import {Card, EmptyCard, CardBack} from "./cards"
import {EventsLog} from "./EventsLog"
import {GameResult} from "./GameResult"
import {PlayerDeckSummary} from "./PlayerDeck"
import {PlayerLogin} from "./PlayerLogin"
import {CurrentPlayer, Players} from "./Players"

class StartCompetition extends React.Component<StartCompetitionListener, void> {
  constructor() {
    super()
  }
  render() {
    const props = this.props
    return (
      <input type="button"
        onClick = { e => props.onStartCompetition() }
        value="Create Competition"></input>
    )
  }
}

export interface GameProps<G extends GameState> extends PlayerDeckSelectionListener, PlayCardListener {
  game: G
}

export class GameTable extends React.Component<{ game:ActiveGameState }, void> {
  render() {
    const gm = this.props.game
    let elemEvs = gm.moves.map((mv, idx) =>
      <div key={idx}>
        <Card card={mv.card} />
        <span>{mv.player.name}</span>
      </div>
    )
    elemEvs = elemEvs.concat(gm.nextPlayers.map((p, idx) =>
      <div key={idx + gm.moves.length}>
        <EmptyCard />
        <span>{p.name}</span>
      </div>
    ))
    elemEvs.push(
      <div key={gm.nextPlayers.length + gm.moves.length + 1}>
        <Card card={gm.briscolaCard} classes={[cssClasses.halfCard]} />
        <CardBack classes={[cssClasses.halfCard]}/>
        <span>{gm.deckCardsNumber}</span>
      </div>
    );

    return (
      <div className={cssClasses.cards}>
        {elemEvs}
      </div>
    )
  }
}

export interface BoardProps extends BoardListener {
  board: Model.Board
}

function showCompetitionButton(board: Model.Board): boolean {
  const len = Object.keys(board.competitionSelectedPlayers).length + 1
  return (len >= board.config.minPlayersNumber) && (len <= board.config.maxPlayersNumber)
}

export class PlayerCards extends React.Component<GameProps<ActiveGameState>, void> {
  render() {
    const props = this.props
    const gameId = props.game.self
    const playerState = props.game.playerState
    const elems = playerState.map(ps =>
      ps.cards.map((card, idx) => <div key={idx}><Card card={card} onClick={ () => props.onPlayCard(card) } /></div>)
    ).getOrElse(() => [<span key="0">Player has no cards</span>])

    return (
      <div className={cssClasses.cards}>
        {elems}
        <div>
          <CardBack onClick={ () => props.onPlayerDeck(gameId, true) } />
        </div>
      </div>
    )
  }
}

class Game extends React.Component<GameProps<Model.GameState>, void> {
  render() {
    const props = this.props
    const gm = props.game
    return Model.GameState.fold(gm,
      (gm: Model.ActiveGameState) =>
        <section>
          <GameTable game={gm} />
          <PlayerCards game={gm} onPlayerDeck={props.onPlayerDeck} onPlayCard={props.onPlayCard}/>
        </section>,
      (gm: Model.FinalGameState) =>
        <section>
          <GameResult game={gm} />
        </section>,
      (gm: Model.DroppedGameState) =>
        <section>
          <h1>Dropped Game !!!</h1>
        </section>
    )
  }
}


interface GamesNavProps extends GameListener {
  classes?:string[]
  current:Model.Path
  games:Model.Path[]
}

class GamesNav extends React.Component<GamesNavProps, void> {
  render() {
    const props = this.props
    const games = props.games
    const clss = ([cssClasses.gamesNav].concat( isNull(props.classes) ? [] : props.classes)).join(" ")
    return games.length == 0 ?
      <noscript /> :
      <div className={clss}>
        {games.map( (gm, idx) => {
          const selectedClassName = props.current === gm ? cssClasses.selected : ""
          return <span className={selectedClassName} onClick={ ev => props.onSelectedGame(gm) } key={gm}>{idx}</span>
        })}
      </div>;
  }
}

export class Board extends React.Component<BoardProps, void> {
  render() {
    const props = this.props
    const board = this.props.board;

    const gameSection = board.currentGame.map(gm =>
      <section>
        <Game game={gm} onPlayerDeck={props.onPlayerDeck} onPlayCard={props.onPlayCard} />
        <GamesNav current={gm.self} games={Object.keys(board.activeGames)} onSelectedGame={props.onSelectedGame} />
        <GamesNav current={gm.self} games={Object.keys(board.finishedGames)} onSelectedGame={props.onSelectedGame} />
      </section>
    ).getOrElse(() => <noscript /> )

    const playerDeckDialog = board.viewFlag === Model.ViewFlag.showPlayerCards ?
      board.currentGame.map( gm =>
        Model.GameState.fold(gm,
          (gm) => <PlayerDeckSummary cards={gm.playerState.map(ps => ps.score.cards).getOrElse( () => []) } onClose={ () => props.onPlayerDeck(gm.self, false) } />,
          (gm) => <noscript />,
          (gm) => <noscript /> )
      ).getOrElse( () => <noscript /> ) : <noscript />

    const startCompetitionButton = showCompetitionButton(board) ? <StartCompetition onStartCompetition={props.onStartCompetition} /> : <noscript />
    return board.player.map(pl => (
      <div>
        {playerDeckDialog}
        <CurrentPlayer {...pl} />
        <Players players={board.players}
          selectedPlayers={ board.competitionSelectedPlayers }
          onPlayerSelection={ props.onPlayerSelection } />
        {startCompetitionButton}
        {gameSection}
        <EventsLog events={board.eventsLog}
          onAcceptCompetition={ props.onAcceptCompetition }
          onDeclineCompetition={ props.onDeclineCompetition }
          onSelectedGame={ props.onSelectedGame } />
      </div>
    )).getOrElse(() => (
      <PlayerLogin onCreatePlayer={ props.onCreatePlayer } onPlayerLogin={ props.onPlayerLogin }/>
    ))

  }

}
