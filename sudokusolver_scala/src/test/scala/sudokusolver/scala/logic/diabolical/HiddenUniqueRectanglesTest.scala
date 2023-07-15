package sudokusolver.scala.logic.diabolical

import munit.FunSuite
import sudokusolver.scala.RemoveCandidates
import sudokusolver.scala.logic.assertLogicalSolution

class HiddenUniqueRectanglesTest extends FunSuite:
  test("type 1 test 1") {
    val board =
      """
        |1{24}957{2468}3{26}{2468}
        |{568}7{56}39{24}{68}1{24}
        |{68}{24}3{468}1{2468}597
        |{56}8{156}743{169}{256}{2569}
        |492{16}5{16}783
        |73{156}289{16}4{56}
        |317{89}2{58}4{56}{5689}
        |26{48}{19}3{145}{89}7{589}
        |95{48}{48}67231
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(0, 5, 2, 4),
      RemoveCandidates(3, 2, 6)
    )
    assertLogicalSolution(expected, board, hiddenUniqueRectangles)
  }

  test("type 1 test 2") {
    val board =
      """
        |518472639
        |3{27}6859{127}{127}4
        |4{27}9316{257}{2578}{278}
        |94562{17}3{178}{78}
        |861{79}34{279}{279}5
        |732{19}85{149}{149}6
        |65{47}{127}9{17}8{247}3
        |293{57}48{57}61
        |18{47}{257}63{24579}{24579}{27}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(2, 7, 7),
      RemoveCandidates(8, 6, 7),
      RemoveCandidates(8, 7, 7)
    )
    assertLogicalSolution(expected, board, hiddenUniqueRectangles)
  }

  test("type 2 test 1") {
    val board =
      """
        |5{47}{47}291836
        |{68}3{68}475{29}1{29}
        |{12}{12}9386457
        |{2689}5{678}143{2679}{689}{289}
        |4{26}{368}759{236}{68}1
        |{19}{17}{137}862{379}45
        |3{469}{456}{56}2{48}17{89}
        |{16}8{156}937{56}24
        |7{49}2{56}1{48}{569}{689}3
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(3, 2, 6),
      RemoveCandidates(3, 6, 9)
    )
    assertLogicalSolution(expected, board, hiddenUniqueRectangles)
  }

  test("type 2 test 2") {
    val board =
      """
        |518472639
        |3{27}6859{127}{127}4
        |4{27}9316{257}{258}{278}
        |94562{17}3{178}{78}
        |861{79}34{279}{279}5
        |732{19}85{149}{149}6
        |65{47}{127}9{17}8{247}3
        |293{57}48{57}61
        |18{47}{257}63{24579}{24579}{27}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(8, 6, 7),
      RemoveCandidates(8, 7, 7)
    )
    assertLogicalSolution(expected, board, hiddenUniqueRectangles)
  }

  test("type 2B test 1") {
    val board =
      """
        |{147}2{479}58{469}{679}3{179}
        |35{79}{129}{12}{1269}{679}84
        |{14}867{34}{349}{59}2{159}
        |{27}48{23}9{237}156
        |5{17}{127}6{27}8{39}4{39}
        |963{14}5{14}278
        |{247}9{247}{234}6581{37}
        |6{17}{1457}8{1347}{134}{3457}92
        |83{12457}{1249}{1247}{12479}{457}6{57}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(7, 2, 7),
      RemoveCandidates(7, 5, 4)
    )
    assertLogicalSolution(expected, board, hiddenUniqueRectangles)
  }

  test("type 2B test 2") {
    val board =
      """
        |518472639
        |3{27}6859{127}{127}4
        |4{27}9316{257}{258}{278}
        |94562{17}3{178}{78}
        |861{79}34{279}{279}5
        |732{19}85{149}{149}6
        |65{47}{127}9{17}8{247}3
        |293{57}48{57}61
        |18{47}{257}63{24579}{2459}{27}
        |""".stripMargin.replace("\n", "")
    val expected = Seq(RemoveCandidates(8, 6, 7))
    assertLogicalSolution(expected, board, hiddenUniqueRectangles)
  }