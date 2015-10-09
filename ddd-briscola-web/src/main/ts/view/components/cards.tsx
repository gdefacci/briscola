/// <reference path='../../_all.ts' />

namespace View.Components {

  interface CardProps extends Clickable {
    classes?: string[]
    key?: string | number
    card: Model.Card
  }

  export class Card extends React.Component<CardProps, void> {
    render() {
      const props = this.props
      const card = props.card;
      const clss = Std.isNull(props.classes) ? "" : props.classes.join(" ")
      const className = `${cssClasses.card} ${clss} card_${Model.Seed[card.seed]}_${card.number}`
      const extra = Std.isNull(props.key) ? {} : { key: props.key };
      const onClick = (ev) => {
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

  interface EmptyCardProps extends Clickable {
    classes?: string[]
  }

  export class EmptyCard extends React.Component<EmptyCardProps, void> {
    render() {
      const props = this.props
      const clss = Std.isNull(props.classes) ? "" : props.classes.join(" ")
      return (
        <img className={`${cssClasses.card} ${clss} ${cssClasses.emptyCard}`}
          onClick={ev => !Std.isNull(props.onClick) ? props.onClick() : undefined } />
      );
    }
  }

  export class CardBack extends React.Component<EmptyCardProps, void> {
    render() {
      const props = this.props
      const clss = Std.isNull(props.classes) ? "" : props.classes.join(" ")
      return (
        <img className={`${cssClasses.card} ${clss} ${cssClasses.cardBack}`}
          onClick={ev => !Std.isNull(props.onClick) ? props.onClick() : undefined } />
      );
    }
  }

}