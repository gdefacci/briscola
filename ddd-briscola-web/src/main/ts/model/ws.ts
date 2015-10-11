namespace Model.Ws {
  
  export type Path = string

  export interface EventAndState<E, S> {
    event: E
    state: S
  }
  
  export interface Collection<T> {
    members:T[]
  }
  
  export interface DomainEvent {
    kind:string
  }
  
  export interface State {
  }
  
  export interface SiteMap {
    players:Path
    playerLogin:Path
  }

}