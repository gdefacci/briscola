import {PlayerLoginListener} from "./listeners"

export class PlayerLogin extends React.Component<PlayerLoginListener, void> {
  static playerNameRef = "playerName"
  static playerPasswordRef = "playerPasswordRef"
  constructor() {
    super()
  }
  private inputRef(nm: string): HTMLInputElement {
    return this.refs[nm] as HTMLInputElement;
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
