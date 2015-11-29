import {isNull, Arrays, JsMap, Option} from "flib"
import {Player, GameState, ActiveGameState} from "ddd-briscola-model"
import * as Model from "ddd-briscola-model"
import {GameListener, BoardListener, PlayerSelectionListener, StartCompetitionListener, PlayerLoginListener, PlayerDeckSelectionListener, PlayCardListener} from "./listeners"
import cssClasses from "./cssClasses"
import {Card, EmptyCard, CardBack} from "./cards"
import {TabPane, TabPaneItem} from "./TabPane"
import {EventsLog} from "./EventsLog"

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

class PlayerLogin extends React.Component<PlayerLoginListener, void> {
  static playerNameRef = "playerName"
  static playerPasswordRef = "playerPasswordRef"
  constructor() {
    super()
  }
  private inputRef(nm: string): HTMLInputElement {
    return React.findDOMNode<HTMLInputElement>(this.refs[nm]);
  }
  private playerName(): HTMLInputElement {
    return this.inputRef(PlayerLogin.playerNameRef);
  }
  private playerPassword(): HTMLInputElement {
    return this.inputRef(PlayerLogin.playerPasswordRef);
  }
  render() {
    const props = this.props
    return (
      <div className="{cssClasses.createPlayer} my-exp-style">
        <input ref={PlayerLogin.playerNameRef} type="text" ></input>
        <input ref={PlayerLogin.playerPasswordRef} type="password" ></input>
        <input type="button"
          onClick = { e => props.onCreatePlayer(this.playerName().value, this.playerPassword().value) }
          value="Create Player"></input>
        <input type="button"
          onClick = { e => props.onPlayerLogin(this.playerName().value, this.playerPassword().value) }
          value="Log in"></input>
      </div>
    )
  }
}

class CurrentPlayer extends React.Component<Player, void> {
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

interface PlayersProps extends PlayerSelectionListener {
  players: Player[]
  selectedPlayers: JsMap<boolean>
}

class Players extends React.Component<PlayersProps, void> {
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

export interface GameProps<G extends GameState> {
  game: G
}

export interface EditableGameProps<G extends GameState> extends GameProps<G>, PlayerDeckSelectionListener, PlayCardListener {
  game: G
}

export class GameTable extends React.Component<GameProps<ActiveGameState>, void> {
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

export class PlayerCards extends React.Component<EditableGameProps<ActiveGameState>, void> {
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

export interface BoardProps extends BoardListener {
  board: Model.Board
}

function showCompetitionButton(board: Model.Board): boolean {
  const len = Object.keys(board.competitionSelectedPlayers).length + 1
  return (len >= board.config.minPlayersNumber) && (len <= board.config.maxPlayersNumber)
}

function playersGameResultTabPaneItems(gameRes:Model.PlayersGameResult):TabPaneItem[] {
  return gameRes.playersOrderByPoints.map( pl => {
    const r:TabPaneItem = {
      activator:(selected:boolean, selectItem:() => void, idx:number) => {
        return <span key={idx}><button onClick={ ev => selectItem() }>{pl.player.name}</button></span>
      },
      content:() => {
        return (
          <div>
            {playerDeckSummaryCards(pl.score.cards)}
            <div className={cssClasses.playerDeckSummary}>
              <span>{pl.points}</span>
            </div>
          </div>
        );
      }
    }
    return r;
  })
}

function teamGameResultTabPaneItems(gameRes:Model.TeamsGameResult):TabPaneItem[] {
  return gameRes.teamsOrderByPoints.map( teamScore => {
    const total = teamScore.points
    const r:TabPaneItem = {
      activator:(selected:boolean, selectItem:() => void, idx:number) => {
        return <span key={idx}><button onClick={ ev => selectItem() }>{teamScore.teamName} : {teamScore.players.map( pl => pl.name ).join(", ")}</button></span>
      },
      content:() => {
        return (
          <div>
            {playerDeckSummaryCards(teamScore.cards)}
            <div className={cssClasses.playerDeckSummary}>
              <span>{total}</span>
            </div>
          </div>
        );
      }
    }
    return r;
  })
}

function playersResultsTabPaneItems(gm: Model.FinalGameState):TabPaneItem[] {
  const gmRes = gm.gameResult
  if (gmRes instanceof Model.TeamsGameResult) return teamGameResultTabPaneItems(gmRes)
  else if (gmRes instanceof Model.PlayersGameResult) return playersGameResultTabPaneItems(gmRes)
  throw new Error("unexcepted "+gm)
}

class Game extends React.Component<EditableGameProps<Model.GameState>, void> {
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
          <TabPane panes={playersResultsTabPaneItems(gm)} />
        </section>,
      (gm: Model.DroppedGameState) =>
        <section>
          <h1>Dropped Game !!!</h1>
        </section>
    )
  }
}

interface PlayerDeckProps {
  cards:Model.Card[]
  onClose():void
}

function playerDeckSummaryCards(cards:Model.Card[]):React.ReactElement<any>[] {
  const cardsBySeed = Arrays.groupBy(cards, c => Model.Seed[c.seed])
  return Object.keys(cardsBySeed).map( (seed, idx) => {
    const cards = cardsBySeed[seed];
    cards.sort( (ca, cb) => {
      const r = cb.points - ca.points
      return (r !== 0) ? r : (cb.number - ca.number);
    });
    const total = Arrays.foldLeft(cards, 0, (acc, card) => acc + card.points);
    return (
      <div key={idx} className={cssClasses.playerDeckSummary}>
        <span>{total}</span>
        {cards.map( (card, idx) => <Card key={idx} card={card} /> )}
      </div>
    );
  })
}

class PlayerDeckSummary extends React.Component<PlayerDeckProps, void> {
  render() {
    const props = this.props
    const cardsBySeed = Arrays.groupBy(props.cards, c => Model.Seed[c.seed])
    const cards = playerDeckSummaryCards(props.cards)
    const total = Arrays.foldLeft(props.cards, 0, (acc, card) => acc + card.points)
    return (
      <div>
      <div className={cssClasses.playerDeckLayer}>
        <div>
          {cards}
          <div className={cssClasses.playerDeckSummary}>
            <span>{total}</span>
          </div>
        </div>
        <div>
          <button onClick={props.onClose}>Close</button>
        </div>
      </div>
      </div>
    );
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
          return <span className={selectedClassName} onClick={ ev => props.onSelectedGame(gm) }>{idx}</span>
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
    ).getOrElse(() =>
      <noscript />
    )

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
