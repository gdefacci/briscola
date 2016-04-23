import {Arrays} from "flib"
import * as Model from "ddd-briscola-model"
import {Card} from "./cards"
import cssClasses from "./cssClasses"

export interface PlayerDeckProps {
  cards:Model.Card[]
  onClose():void
}

/*
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
*/

export class PlayerDeckSummaryCards  extends React.Component<{ cards:Model.Card[] }, void> {
  render() {
    const cards = this.props.cards
    const cardsBySeed = Arrays.groupBy(cards, c => Model.Seed[c.seed])
    const items = Object.keys(cardsBySeed).map( (seed, idx) => {
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
    return <div>{items}</div>
  }
}

export class PlayerDeckSummary extends React.Component<PlayerDeckProps, void> {
  render() {
    const props = this.props
    const cardsBySeed = Arrays.groupBy(props.cards, c => Model.Seed[c.seed])
    const total = Arrays.foldLeft(props.cards, 0, (acc, card) => acc + card.points)
    return (
      <div>
      <div className={cssClasses.playerDeckLayer}>
        <PlayerDeckSummaryCards cards={props.cards} />
        <div>
          <span>{total}</span>
        </div>
        <div>
          <button onClick={props.onClose}>Close</button>
        </div>
      </div>
      </div>
    );
  }
}
