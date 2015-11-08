import {CurrentPlayer, GameState, ActiveGameState, FinalGameState, gameStateChoice, GameStarted, byKindChoice, 
  EventAndState, GameEventAndState, eventAndStateChoice, briscolaEventChoice} from "../index"
  
import {JsMap} from "flib"
import {fetch, ByUrlCache, fetchChoose} from "rest-fetch"
import * as TestData from "./testData"

function assert(b: boolean) {
  if (!b) {
    throw new Error("Assertion error");
  }
}

function testFetchOpts(data: JsMap<any>) {
  const testHttpCache = new ByUrlCache()

  JsMap.forEach(data, (k, v) => {
    testHttpCache.store(new Request(k), Promise.resolve(v))
  })

  return {
    httpCache() {
      return testHttpCache;
    }
  }
}

function test1() {
  const opts = testFetchOpts({
    "http://localhost:8080/app/players/3": {
      "self": "http://localhost:8080/app/players/3",
      "name": "Ronny",
      "webSocket": "ws://localhost:8080/app/ws/players/3",
      "createCompetition": "http://localhost:8080/app/competitions/player/3"
    }
  })

  return fetch(CurrentPlayer, opts).from("http://localhost:8080/app/players/3").then(pl => {
    assert(pl.name === "Ronny")
    assert(pl.createCompetition === "http://localhost:8080/app/competitions/player/3")
  })
}



function test2() {
  const opts = testFetchOpts(TestData.testData2)

  const p1 = fetch(ActiveGameState, opts).from("http://localhost:8080/app/games/1").then(gm => {
    assert(gm.self === "http://localhost:8080/app/games/1")
  })

  const p2 = fetchChoose(gameStateChoice, opts).from("http://localhost:8080/app/games/1").then(gm => {
    assert(gm instanceof ActiveGameState)
  })

  return Promise.all<any>([p1, p2])
}

function test3a() {
  const opts = testFetchOpts(TestData.testData2)
  const p1 = fetchChoose(briscolaEventChoice, opts).fromObject(TestData.eventAndState1.event).then(ev => {
    assert(ev.eventName === "gameStarted")
    if (ev instanceof GameStarted) {
      checkGameState1(ev.game)
    } else return Promise.reject("not a GameStarted")
  }, err => {
    console.log(`error ${err}`)
  })

  const p1a = fetchChoose(gameStateChoice, opts).fromObject(TestData.eventAndState1.state).then(st => {
    checkGameState1(st)
  }, err => {
    console.log(`error ${err}`)
  })

  return Promise.all([p1,p1a])
}

function checkGameState1(gm: GameState) {
    if (gm instanceof ActiveGameState) {
      assert(gm.players.length === 2)
      assert(gm.nextPlayers.length === 2)
      assert(gm.moves.length === 0)
      assert(gm.playerState !== undefined)
      assert(gm.playerState.isDefined())
      assert(gm.playerState.map( ps => ps.cards.length === 3).getOrElse( () => false))
      return Promise.resolve(gm)
    } else {
      return Promise.reject("not a ActiveGameState")
    }
  }

function checkGameState2(gm: GameState) {
    if (gm instanceof FinalGameState) {
      return Promise.resolve(gm)
    } else {
      return Promise.reject("not a FinalGameState")
    }
  }

function test3() {
  const opts = testFetchOpts(TestData.testData2)

  return fetch(GameEventAndState, opts).fromObject(TestData.eventAndState1).then(es => {
    const ev = es.event
    assert(ev.eventName === "gameStarted")
    if (ev instanceof GameStarted) {
      checkGameState1(ev.game)
    } else return Promise.reject("not a GameStarted")
    checkGameState1(es.game)
  })
}

function test3b() {
  const opts = testFetchOpts(TestData.testData2)

  return fetchChoose(eventAndStateChoice, opts).fromObject(TestData.eventAndState1).then(es => {
    const ev = es.event
    assert(ev.eventName === "gameStarted")
    if (ev instanceof GameStarted) {
      checkGameState1(ev.game)
    } else return Promise.reject("not a GameStarted")
    checkGameState1(es.game)
  })
}

function test4() {
  const opts = testFetchOpts(TestData.testData2)

  return fetch(GameEventAndState, opts).fromObject(TestData.eventAndState2).then(es => {
    const ev = es.event
    assert(ev.eventName === "cardPlayed")
    checkGameState2(es.game)
  })
}

function test4a() {
  const opts = testFetchOpts(TestData.testData2)

  return fetchChoose(eventAndStateChoice, opts).fromObject(TestData.eventAndState2).then(es => {
    const ev = es.event
    assert(ev.eventName === "cardPlayed")
    checkGameState2(es.game)
  })
}

Promise.all<any>([
  test1(),
  test2(),
  test3a(),
  test3(),
  test3b(),
  test4(),
  test4a()
]).then(u => document.body.appendChild(document.createTextNode("tests done")))