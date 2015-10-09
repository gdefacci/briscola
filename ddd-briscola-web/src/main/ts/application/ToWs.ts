namespace Application.ToWs {

  export function card(m:Model.Card):Model.Ws.Card {
    return {
      number: m.number,
      seed: Model.Seed[m.seed]
    }  
  }   


}