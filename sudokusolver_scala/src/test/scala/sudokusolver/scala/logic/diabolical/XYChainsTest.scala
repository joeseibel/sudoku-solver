package sudokusolver.scala.logic.diabolical

import munit.FunSuite
import scalax.collection.immutable.Graph
import sudokusolver.scala.logic.assertLogicalSolution
import sudokusolver.scala.*

class XYChainsTest extends FunSuite:
  test("test toDOT") {
    val a = UnsolvedCell(0, 0) -> SudokuNumber.TWO
    val b = UnsolvedCell(0, 0) -> SudokuNumber.SIX
    val c = UnsolvedCell(0, 4) -> SudokuNumber.TWO
    val graph = Graph.from(Seq(StrengthEdge(a, b, Strength.STRONG), StrengthEdge(a, c, Strength.WEAK)))
    val actual = graph.toDOTXYChains.replace("\t", "  ")
    val expected =
      """graph {
        |  "[0,0] : 2" -- "[0,0] : 6"
        |  "[0,0] : 2" -- "[0,4] : 2" [style = dashed]
        |}""".stripMargin
    assertEquals(actual, expected)
  }

  test("test 1") {
    val board =
      """
        |{26}8{245}1{29}3{59}7{456}
        |{37}9{24}5{27}6{18}{14}{348}
        |{37}{56}14{79}8{359}2{356}
        |578241639
        |143659782
        |926837451
        |{68}379{16}52{14}{48}
        |{268}{56}{25}3{16}4{18}97
        |419782{35}6{35}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(0, 2, 2, 5),
      RemoveCandidates(1, 8, 4),
      RemoveCandidates(2, 6, 5),
      RemoveCandidates(2, 8, 5),
      RemoveCandidates(7, 0, 6)
    )
    assertLogicalSolution(expected, board, xyChains)
  }

  test("test 2") {
    val board =
      """
        |{48}92{145}{18}{158}376
        |{478}1{68}{24679}3{2689}5{28}{248}
        |3{567}{568}{2467}{2678}{268}19{248}
        |93{46}85{26}7{24}1
        |{78}{567}{1568}3{126}4{689}{258}{289}
        |2{56}{14568}{16}97{68}{458}3
        |689{257}{27}341{57}
        |523{179}4{189}{89}6{789}
        |147{569}{68}{5689}23{589}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(1, 0, 8),
      RemoveCandidates(1, 5, 8),
      RemoveCandidates(1, 8, 8),
      RemoveCandidates(2, 2, 6),
      RemoveCandidates(4, 2, 6),
      RemoveCandidates(4, 7, 2),
      RemoveCandidates(5, 2, 6)
    )
    assertLogicalSolution(expected, board, xyChains)
  }

  test("test 3") {
    val board =
      """
        |931672458
        |672854193
        |{58}4{58}913762
        |{28}{169}{48}5{349}7{369}{128}{49}
        |3{69}{45}{12}{49}8{569}{12}7
        |{258}{19}7{12}{349}6{359}{128}{459}
        |486321{59}7{59}
        |153789246
        |729465831
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(3, 4, 9),
      RemoveCandidates(4, 6, 9)
    )
    assertLogicalSolution(expected, board, xyChains)
  }

  test("test 4") {
    val board =
      """
        |{45}938{24}716{25}
        |286591437
        |{145}7{14}6{234}{34}89{25}
        |{479}{13}{47}2{37}5{69}8{169}
        |{89}{13}546{38}27{19}
        |{78}621{78}9543
        |32{17}9{148}{48}{67}5{46}
        |{17}583{14}6{79}2{49}
        |649752318
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(2, 0, 4),
      RemoveCandidates(2, 4, 3, 4),
      RemoveCandidates(2, 5, 4),
      RemoveCandidates(3, 0, 7, 9),
      RemoveCandidates(3, 1, 3),
      RemoveCandidates(3, 4, 7),
      RemoveCandidates(3, 6, 9),
      RemoveCandidates(3, 8, 1, 6),
      RemoveCandidates(4, 0, 8),
      RemoveCandidates(4, 1, 1),
      RemoveCandidates(4, 5, 3),
      RemoveCandidates(4, 8, 9),
      RemoveCandidates(5, 0, 7),
      RemoveCandidates(5, 4, 8),
      RemoveCandidates(6, 2, 7),
      RemoveCandidates(6, 4, 1, 4),
      RemoveCandidates(6, 6, 6),
      RemoveCandidates(6, 8, 4),
      RemoveCandidates(7, 0, 1),
      RemoveCandidates(7, 4, 4),
      RemoveCandidates(7, 6, 7),
      RemoveCandidates(7, 8, 9)
    )
    assertLogicalSolution(expected, board, xyChains)
  }

  test("test 5") {
    val board =
      """
        |9{246}3{458}{267}1{478}{245}{2578}
        |8{246}{46}{345}{2367}{56}{3479}{1245}{12579}
        |751{348}{23}9{348}6{28}
        |187{35}{36}{56}294
        |{35}{34}{45}792186
        |2{69}{69}148573
        |67{58}913{48}{245}{258}
        |{35}{39}2684{79}{15}{1579}
        |41{89}25763{89}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(0, 8, 8),
      RemoveCandidates(1, 1, 6),
      RemoveCandidates(1, 4, 3, 6),
      RemoveCandidates(6, 8, 8)
    )
    assertLogicalSolution(expected, board, xyChains)
  }