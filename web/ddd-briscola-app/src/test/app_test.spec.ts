import * as Commands from "../lib/Command"

import { ObservableBuffer, expectResolve, TestConfigMap, createApplication, asserThatObservable } from "./TestHelper"
import * as TestData from "./TestData"
import { Observable } from '@reactivex/rxjs';

function givenAStartedApplication(cfg: TestConfigMap, webSocket?: Observable<MessageEvent>) {
  const app = createApplication(cfg, webSocket)
  return app.exec(new Commands.StarApplication()).then(v => app)
}

describe("App sucessfull interactions", () => {

  it("can start application", done => {

    const app = createApplication({
      GET: {
        "/entrypoint": TestData.siteMap
      }
    })
    const out = new ObservableBuffer(app.displayChannel)

    expectResolve(app.exec(new Commands.StarApplication()))(
      () => {
        expect(out.buffer.length).toBe(1)
        done()
      })
  })

  it("issue a create player command", done => {

    expectResolve( givenAStartedApplication({
        GET: {
          "/entrypoint": TestData.siteMap
        },
        POST: {
          "/players": {
            self: "/player/1",
            name: "name",
            webSocket: "/ws/1",
            createCompetition: "/competitions/player/1"
          }
        }
      }).then(app => {
        return app.exec(new Commands.CreatePlayer("name", "psw")).then(() => app)
      })
    )(app => {

      asserThatObservable(app.displayChannel)( boards => {
        expect(boards.length).toBe(2)
        const player = boards[1].player.getOrElse(() => null)
        expect(player).toBeDefined()
        expect(player && player.name).toBe("name")
        done()
      })

    })
  })

})