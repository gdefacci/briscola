import {Clickable} from "./listeners"
import * as Model from "ddd-briscola-model"
import cssClasses from "./cssClasses"
import {isNull} from "flib"

export interface CardProps extends Clickable {
  classes?: string[]
  key?: string | number
  card: Model.Card
}

export class Card extends React.Component<CardProps, void> {
  render() {
    const props = this.props
    const card = props.card;
    const clss = isNull(props.classes) ? "" : props.classes.join(" ")
    const className = `${cssClasses.card} ${clss} card_${Model.Seed[card.seed]}_${card.number}`
    const extra = isNull(props.key) ? {} : { key: props.key };
    const onClick = (ev:React.MouseEvent) => {
      console.log("triggered card onClick")
      props.onClick && props.onClick()
    }
    return (
      <img
        {...extra}
        className={className}
        onClick={onClick} />
    );
  }
}

export interface EmptyCardProps extends Clickable {
  classes?: string[]
}

export class EmptyCard extends React.Component<EmptyCardProps, void> {
  render() {
    const props = this.props
    const clss = isNull(props.classes) ? "" : props.classes.join(" ")
    return (
      <img className={`${cssClasses.card} ${clss} ${cssClasses.emptyCard}`}
        onClick={ev => !isNull(props.onClick) ? props.onClick() : undefined } />
    );
  }
}

export class CardBack extends React.Component<EmptyCardProps, void> {
  render() {
    const props = this.props
    const clss = isNull(props.classes) ? "" : props.classes.join(" ")
    return (
      <img className={`${cssClasses.card} ${clss} ${cssClasses.cardBack}`}
        onClick={ev => !isNull(props.onClick) ? props.onClick() : undefined } />
    );
  }
}
