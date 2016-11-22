import * as Model from "ddd-briscola-model"
import cssClasses from "./cssClasses"
import {TabPane, TabPaneItem} from "./TabPane"
import {PlayerDeckSummaryCards} from "./PlayerDeck"

function playersGameResultTabPaneItems(gameRes:Model.PlayersGameResult):TabPaneItem[] {
  return gameRes.playersOrderByPoints.map( pl => {
    const r:TabPaneItem = {
      activator:(selected:boolean, selectItem:() => void, idx:number) => {
        return <span key={idx}><button onClick={ ev => selectItem() }>{pl.player.name}</button></span>
      },
      content:() => {
        return (
          <div>
            <PlayerDeckSummaryCards cards={pl.score.cards} />
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
            <PlayerDeckSummaryCards cards={teamScore.cards} />
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

export class GameResult extends React.Component<{ game:Model.FinalGameState }, void> {
  render() {
    const props = this.props
    const gm = props.game
    return <TabPane panes={playersResultsTabPaneItems(gm)} />
  }
}
