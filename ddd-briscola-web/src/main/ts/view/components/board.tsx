/// <reference path='../../_all.ts' />

module View.Components {

  import Option = Std.Option

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
        <div className={cssClasses.createPlayer}>
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

  class CurrentPlayer extends React.Component<Model.Player, void> {
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
    players: Model.Player[]
    selectedPlayers: Std.JsMap<boolean>
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

  interface GameProps<G extends Model.GameState> {
    game: G
  }

  interface EditableGameProps<G extends Model.GameState> extends GameProps<G>, PlayerDeckSelectionListener, PlayCardListener {
    game: G
  }

  export class GameTable extends React.Component<GameProps<Model.ActiveGameState>, void> {
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

  export class PlayerCards extends React.Component<EditableGameProps<Model.ActiveGameState>, void> {
    render() {
      const props = this.props
      const playerState = props.game.playerState
      const elems = playerState.map(ps =>
        ps.cards.map((card, idx) => <div key={idx}><Card card={card} onClick={ () => props.onPlayCard(card) } /></div>)
      ).getOrElse(() => [<span key="0">Player has no cards</span>])

      return (
        <div className={cssClasses.cards}>
          {elems}
          <div>
            <CardBack onClick={ () => props.onPlayerDeck(playerState.map(ps => ps.cards).getOrElse(() => [])) } />
          </div>
        </div>
      )
    }
  }

  interface BoardProps extends BoardListener {
    board: Model.Board
  }

  function showCompetitionButton(board: Model.Board): boolean {
    const len = Object.keys(board.competitionSelectedPlayers).length + 1
    return (len >= board.config.minPlayersNumber) && (len <= board.config.maxPlayersNumber)
  }

  class Game extends React.Component<EditableGameProps<Model.GameState>, void> {
    render() {
      const props = this.props
      const gm = props.game
      return Model.GameState.fold(
        (gm: Model.ActiveGameState) =>
          <section>
            <GameTable game={gm} />
            <PlayerCards game={gm} onPlayerDeck={props.onPlayerDeck} onPlayCard={props.onPlayCard}/>
          </section>,
        (gm: Model.FinalGameState) =>
          <section></section>
      )(gm)
    }
  }

  export class Board extends React.Component<BoardProps, void> {
    render() {
      const props = this.props
      const board = this.props.board;

      const gameSection = board.currentGame.map(gm =>
        <Game game={gm} onPlayerDeck={props.onPlayerDeck} onPlayCard={props.onPlayCard} />
      ).getOrElse(() => <noscript />)

      const startCompetitionButton = showCompetitionButton(board) ? <StartCompetition onStartCompetition={props.onStartCompetition} /> : <noscript />
      return board.player.map(pl => (
        <div>
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

}