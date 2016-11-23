import {merge} from "../lib/Util"


describe("ActiveGame state and event", () => {

  it('simple', (done) => {

    const r = merge({
      a:1
    }, {
      b:"aaa"
    })
    expect(r.a ).toBe(1)
    expect(r.b).toBe("aaa")
    done()
  })

  it('overwrite', (done) => {

    const r = merge({
      a:1
    }, {
      a:"aaa"
    })
    expect(r.a ).toBe("aaa")
    done()
  })

  it('array', (done) => {

    const r = merge({
      a:1,
      b:[1,2]
    }, {
      b:[11,22]
    })
    expect(r.a ).toBe(1)
    expect(r.b[0]).toBe(1)
    expect(r.b[1]).toBe(2)
    expect(r.b[2]).toBe(11)
    expect(r.b[3]).toBe(22)
    done()
  })

  it('object', (done) => {

    const r = merge({
      a:1,
      b:{ a:1, b:2}
    }, {
      b:{ c:11}
    })
    expect(r.a ).toBe(1)
    expect(r.b.a).toBe(1)
    expect(r.b.b).toBe(2)
    expect(r.b.c).toBe(11)
    done()
  })

})
