import "reflect-metadata"

import {
  CurrentPlayer, GameState, ActiveGameState, FinalGameState, gameStateChoice, SiteMap, CompetitionState, Card, Seed,
  briscolaEventChoice, eventAndStateChoice, EventAndState, CreatedCompetition, competitionEventChoice, CompetitionEvent
} from "../index"

import { JsMap } from "flib"
import { TestFetcher, ExtraPropertiesStrategy, mapping, Lazy } from "nrest-fetch"
import * as TestData from "./testData"

const promisesMap = TestFetcher.promisesMap
const fetcher = new TestFetcher(ExtraPropertiesStrategy.fail)

describe("ActiveGame state and event", () => {

  const cache1 = promisesMap(JsMap.merge<any>([TestData.testData2, { "event": TestData.eventAndState1.event }]))

  it('success', (done) => {

    fetcher.fetchResource("event", briscolaEventChoice, cache1)(ev => {
      expect(ev.eventName).toBe("gameStarted")
      checkGameState1(ev.game)
      done()
    })

  });

})

describe("SiteMap", () => {

  const cache1 = promisesMap({
    siteMap: {
      "players": "http://localhost:8080/app/players",
      "playerLogin": "http://localhost:8080/app/players/login"
    }
  })

  it('success', (done) => {

    fetcher.fetchResource("siteMap", mapping(SiteMap), cache1)((sm: SiteMap) => {
      expect(sm.players).toBe("http://localhost:8080/app/players")
      expect(sm.playerLogin).toBe("http://localhost:8080/app/players/login")
      done()
    })

  });

})


describe("Competition Start Event", () => {

  const cache1 = promisesMap(JsMap.merge([TestData.testData2]))

  it('success state', (done) => {

    fetcher.fetchObject(TestData.competionStartEvent.state, mapping(CompetitionState), cache1)((s: CompetitionState) => {
      expect(s instanceof CompetitionState).toBe(true)
      done()
    })
  })

  //function bip(a:any, b:(a:any) => void) {}


  it('success event', (done) => {

    fetcher.fetchObject(TestData.competionStartEvent.event, mapping(CreatedCompetition), cache1)((s: CreatedCompetition) => {
      expect(s instanceof CreatedCompetition).toBe(true)
      done()
    })
  })

  it('success event choice', (done) => {

    fetcher.fetchObject(TestData.competionStartEvent.event, competitionEventChoice, cache1)((s: CompetitionEvent) => {
      expect(s instanceof CreatedCompetition).toBe(true)
      done()
    })
  })


  it('success event and state', (done) => {

    fetcher.fetchObject(TestData.competionStartEvent, eventAndStateChoice, cache1)((es: EventAndState) => {
      expect(es.event instanceof CreatedCompetition).toBe(true)
      expect(es["competition"]).toBeDefined()
      done()
    })

  });

})

describe("ActiveGame state and event", () => {

  const cache1 = promisesMap(JsMap.merge<any>([TestData.testData2, TestData.eventAndState1]))

  it('success', (done) => {

    fetcher.fetchResource("event", briscolaEventChoice, cache1)(ev => {
      expect(ev.eventName).toBe("gameStarted")
      checkGameState1(ev.game)
      done()
    })

    fetcher.fetchResource("state", gameStateChoice, cache1)(st => {
      checkGameState1(st)
      done()
    })

  });

})

describe("CurrentPlayer", () => {

  const cache1 = promisesMap({
    "http://localhost:8080/app/players/3": {
      "self": "http://localhost:8080/app/players/3",
      "name": "Ronny",
      "webSocket": "ws://localhost:8080/app/ws/players/3",
      "createCompetition": "http://localhost:8080/app/competitions/player/3"
    }
  })

  it('success', (done) => {
    fetcher.fetchResource("http://localhost:8080/app/players/3", Lazy.mapping(() => CurrentPlayer), cache1)(pl => {
      expect(pl instanceof CurrentPlayer).toBe(true)
      expect(pl.name).toBe("Ronny")
      done()
    })
  });

})

describe("ActiveGameState", () => {

  const cache1 = promisesMap(JsMap.merge<any>([TestData.testData2, TestData.testData2]))

  it('success', (done) => {
    fetcher.fetchResource("http://localhost:8080/app/games/1", mapping(ActiveGameState), cache1)(pl => {
      expect(pl instanceof ActiveGameState).toBe(true)
      expect(pl.self).toBe("http://localhost:8080/app/games/1")
      done()
    })
  });

})

describe("FinalGameState", () => {

  const cache1 = promisesMap(JsMap.merge<any>([TestData.testData2, TestData.finishedGameState]))

  it('success', (done) => {

    fetcher.fetchResource("state", gameStateChoice, cache1)(st => {
      checkGameState2(st)
      done()
    })

  });

})

describe("Card", () => {

  const cache1 = promisesMap({
    "card":{
        "number" : 7,
        "seed" : "bastoni",
        "points" : 0
      }
  })

  it('success', (done) => {

    fetcher.fetchResource("card", mapping(Card), cache1)(crd => {
      expect(crd.number).toBe(7)
      expect(crd.seed).toBe(Seed.bastoni)
      expect(crd.points).toBe(0)
      done()
    })

  });

})


function checkGameState1(gm: GameState) {
  if (gm instanceof ActiveGameState) {
    expect(gm.players.length).toBe(2)
    expect(gm.nextPlayers.length).toBe(2)
    expect(gm.moves.length).toBe(0)
    expect(gm.playerState).toBeDefined()
    expect(gm.playerState.isDefined()).toBe(true)
    expect(gm.playerState.map(ps => ps.cards.length == 3).getOrElse(() => false)).toBe(true)
  } else {
    return fail("not a ActiveGameState")
  }
}

function checkGameState2(gm: GameState) {
  expect(gm instanceof FinalGameState).toBe(true)
}