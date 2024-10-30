package sudokusolver.scala.logic.tough

import munit.FunSuite
import scalax.collection.edges.UnDiEdgeImplicits
import scalax.collection.immutable.Graph
import sudokusolver.scala.{RemoveCandidates, SudokuNumber, UnsolvedCell}
import sudokusolver.scala.logic.assertLogicalSolution

class SimpleColoringTest extends FunSuite:
  test("test toDOT") {
    val a = UnsolvedCell(0, 0)
    val b = UnsolvedCell(0, 3)
    val graph = Graph.from(Seq(a ~ b))
    val actual = graph.toDOT(SudokuNumber.ONE).replace("\t", "  ")
    val expected =
      """graph 1 {
        |  "[0,0]" -- "[0,3]"
        |}""".stripMargin
    assertEquals(actual, expected)
  }

  test("rule 2 test 1") {
    val board =
      """
        |{145}{15}7{25}836{149}{1249}
        |{145}397{25}68{14}{124}
        |826419753
        |64{25}19{25}387
        |{159}8{12}367{245}{149}{1459}
        |{19}73{25}48{25}6{19}
        |39{15}87{14}{45}26
        |7649{25}{25}138
        |2{15}863{14}97{45}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(0, 1, 5),
      RemoveCandidates(0, 3, 5),
      RemoveCandidates(1, 0, 5),
      RemoveCandidates(3, 5, 5),
      RemoveCandidates(4, 0, 5),
      RemoveCandidates(5, 6, 5),
      RemoveCandidates(6, 2, 5),
      RemoveCandidates(7, 4, 5),
      RemoveCandidates(8, 8, 5)
    )
    assertLogicalSolution(expected, board, simpleColoringRule2)
  }

  test("rule 2 test 2") {
    val board =
      """
        |2{79}{38}{38}41{79}56
        |4{379}56{78}2{789}1{37}
        |{78}16{37}95{278}{23}4
        |35{78}12964{78}
        |142{78}6{37}59{38}
        |{78}695{38}4{27}{23}1
        |584216379
        |92{37}4{37}8165
        |6{37}195{37}482
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(1, 8, 7),
      RemoveCandidates(2, 0, 7),
      RemoveCandidates(2, 3, 7),
      RemoveCandidates(3, 2, 7),
      RemoveCandidates(4, 5, 7),
      RemoveCandidates(5, 6, 7),
      RemoveCandidates(7, 4, 7),
      RemoveCandidates(8, 1, 7)
    )
    assertLogicalSolution(expected, board, simpleColoringRule2)
  }

  test("rule 2 test 3") {
    val board =
      """
        |4{279}{259}8{279}6{25}13
        |{257}86{27}134{25}9
        |{23}{239}1{29}45867
        |{357}1{35}468{37}92
        |{27}{279}83{279}1645
        |64{239}{279}5{27}{37}81
        |1546{237}{27}9{237}8
        |9{23}75841{23}6
        |86{23}1{237}9{25}{2357}4
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(0, 2, 9),
      RemoveCandidates(0, 4, 9),
      RemoveCandidates(2, 1, 9),
      RemoveCandidates(4, 1, 9),
      RemoveCandidates(5, 3, 9)
    )
    assertLogicalSolution(expected, board, simpleColoringRule2)
  }

  test("rule 2 test 4") {
    val board =
      """
        |289{16}{46}{14}375
        |364{57}9{57}812
        |517283964
        |893{457}2{457}6{45}1
        |145836729
        |726{19}{45}{19}{45}83
        |451378296
        |{69}72{4569}1{459}{45}38
        |{69}38{4569}{56}21{45}7
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(5, 6, 5),
      RemoveCandidates(8, 4, 5),
      RemoveCandidates(8, 7, 5)
    )
    assertLogicalSolution(expected, board, simpleColoringRule2)
  }

  test("rule 4 test 1") {
    val board =
      """
        |{145}{15}7{25}836{149}{1249}
        |{145}397{25}68{14}{124}
        |826419753
        |64{25}19{25}387
        |{159}8{125}367{245}{149}{1459}
        |{19}73{25}48{25}6{19}
        |39{15}87{14}{45}26
        |7649{25}{25}138
        |2{15}863{14}97{45}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(4, 0, 5),
      RemoveCandidates(4, 2, 5)
    )
    assertLogicalSolution(expected, board, simpleColoringRule4)
  }

  test("rule 4 test 2") {
    val board =
      """
        |2{3579}{3578}{378}41{789}{35}6
        |4{3579}{3578}6{3578}2{789}1{378}
        |{78}16{378}9{357}{278}{235}4
        |3{57}{578}12964{78}
        |142{378}6{37}59{378}
        |{78}695{378}4{278}{23}1
        |584216379
        |92{37}4{37}8165
        |6{37}19{357}{357}482
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(1, 4, 3),
      RemoveCandidates(1, 8, 8)
    )
    assertLogicalSolution(expected, board, simpleColoringRule4)
  }

  test("rule 4 test 3") {
    val board =
      """
        |12845{37}{37}96
        |{37}46{37}91285
        |9{37}582641{37}
        |{678}{67}35{678}2149
        |{678}91{367}4{37}{68}52
        |4521{68}9{68}{37}{37}
        |{36}{36}4{27}159{27}8
        |287934561
        |519{267}{67}8{37}{237}4
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(3, 4, 7),
      RemoveCandidates(4, 0, 7),
      RemoveCandidates(8, 3, 7)
    )
    assertLogicalSolution(expected, board, simpleColoringRule4)
  }

  test("rule 4 test 4") {
    val board =
      """
        |4{378}{2378}956{23}{238}1
        |6{35}9{24}18{2345}{234}7
        |1{58}{28}37{24}{2456}{2468}9
        |316{24}8975{24}
        |824537196
        |7956{24}18{24}3
        |2{34}{13}7659{134}8
        |9{367}{137}8{24}{24}{36}{1367}5
        |5{4678}{78}193{246}{2467}{24}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(RemoveCandidates(1, 7, 2, 4))
    assertLogicalSolution(expected, board, simpleColoringRule4)
  }

  test("rule 4 test 5") {
    val board =
      """
        |89{67}2{67}4351
        |{457}12{56}{5679}3{469}{467}8
        |3{46}{57}1{579}8{29}{27}{46}
        |{245}{24}9817{2456}{246}3
        |631{45}{45}2789
        |{2457}8{457}936{245}1{45}
        |9537{46}18{46}2
        |{24}{246}{46}385197
        |178{46}29{456}3{456}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(RemoveCandidates(1, 7, 6))
    assertLogicalSolution(expected, board, simpleColoringRule4)
  }

  test("rule 4 test 6") {
    val board =
      """
        |{38}62945{78}{1378}{178}
        |154378692
        |7{38}91624{358}{58}
        |62{57}831{57}49
        |{89}{789}34562{178}{178}
        |41{58}297{58}63
        |5{78}16239{78}4
        |24{68}7193{58}{568}
        |{39}{39}{67}58412{67}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(0, 7, 7, 8),
      RemoveCandidates(2, 7, 8),
      RemoveCandidates(4, 8, 7)
    )
    assertLogicalSolution(expected, board, simpleColoringRule4)
  }