import { App, Commands } from "ddd-briscola-app"
import { createBoardCommandListener } from "./lib/listeners"
import { Board } from "./lib/Board"

import './styles/App.styl';

let app: App

const reactContainer = "react-container"

export function main() {
  app = App.create("site-map")
  const el = document.getElementById(reactContainer)
  if (el !== null) {
    app.displayChannel.subscribe(board => {
      const boardProps = createBoardCommandListener(board, (cmd) => app.exec(cmd))
      console.log("boardProps ")
      console.log(boardProps)
      ReactDOM.render(<Board {...boardProps}></Board>, el)
    })
    app.exec(new Commands.StarApplication())
  } else {
     alert("cant find element " + reactContainer)
  }
}

window.addEventListener("load", (event) => main())