package org.obl.brisola.webtest

import org.scalatest.FunSuite
import org.obl.briscola.web.jsonDecoders
import org.obl.briscola.web.util.ArgonautHttp4sDecodeHelper
import org.obl.briscola.presentation.Input
import org.obl.raz.Path
import org.obl.briscola.competition.CompetitionStartDeadline.AllPlayers
import org.obl.briscola.competition.SingleMatch
import org.obl.raz.RelativePath

class ParseTest extends FunSuite {

  test("create competition") {

    val cont = """
{
  "players":["/a/1"],
  "kind":"single-match",
  "deadline":"all-players"
}
"""

    val exp = Input.Competition(Input.Players(Set(RelativePath / "a" / "1")), SingleMatch, AllPlayers)

    assert(exp == ArgonautHttp4sDecodeHelper.decode(cont)(jsonDecoders.competitionDecoder).toOption.get)
    assert(Input.Players(Set()) == ArgonautHttp4sDecodeHelper.decode("[]")(jsonDecoders.gamePlayersDecoder).toOption.get)
    assert(SingleMatch == ArgonautHttp4sDecodeHelper.decode(""""single-match"""")(jsonDecoders.matchKindDecoder).toOption.get)
    assert(AllPlayers == ArgonautHttp4sDecodeHelper.decode(""""all-players"""")(jsonDecoders.competitionStartDeadlineDecoder).toOption.get)

  }

}