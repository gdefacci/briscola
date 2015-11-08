import ReactElement = React.ReactElement
import {isNull} from "flib"

export interface TabPaneProps {
  classes?:TabPaneClasses
  panes:TabPaneItem[]
}

export interface TabPaneClasses {
  container?:string
  mainArea?:string
  activatorsContainer?:string
}

export interface TabPaneItem {
  activator(selected:boolean, selectItem:() => void, index:number):ReactElement<any>
  content(index:number):ReactElement<any>
}

export interface TabPaneState {
  current:number  
}

export class TabPane extends React.Component<TabPaneProps, TabPaneState> {
  state = {
    current:0  
  }
  render() {
    const props = this.props
    const currIdx = this.state.current
    const curr = props.panes[currIdx]
    const mainArea:ReactElement<any> = isNull(curr) ? (<div>missing pane</div>) : (curr.content(currIdx));
    const activators:ReactElement<any>[] = props.panes.map( (pane, idx) => 
      pane.activator((idx === currIdx), () => { this.setState({ current:idx }) }, idx )
    )
    return (
      <div className={props.classes && props.classes.container || ""}>
        <div className={props.classes && props.classes.mainArea || ""}>
          {mainArea}
        </div>
        <div className={props.classes && props.classes.activatorsContainer || ""}>
          {activators}
        </div>
      </div>
    );  
  }
}



