/// <reference path='../../_all.ts' />

module View.Components {

  import App = Application.App
  
  let app:App
  
  const reactContainer = "react-container"
  
  export function main() {
      app = App.create("site-map")
      const el = document.getElementById(reactContainer)
      app.displayChannel.subscribe( board => {
        const boardProps = View.createBoardCommandListener(board, (cmd) => app.exec(cmd))
        console.log("boardProps ")
        console.log(boardProps)
        React.render(<Board {...boardProps}></Board>, el)
      })
  }
  
}