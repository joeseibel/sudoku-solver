package sudokusolver.scala.logic.tough

import munit.FunSuite
import sudokusolver.scala.RemoveCandidates
import sudokusolver.scala.logic.assertLogicalSolution

class YWingTest extends FunSuite:
  test("test 1") {
    val board =
      """
        |9{38}{1368}24{1378}{57}{58}{568}
        |{478}5{48}69{78}231
        |{13678}2{1368}{18}5{1378}{47}9{468}
        |{1468}9{14568}7{16}{48}32{458}
        |{148}{48}29356{148}7
        |{13468}7{134568}{48}{16}29{1458}{458}
        |{48}69{145}2{14}{1458}73
        |51{348}{34}79{48}62
        |2{34}7{1345}86{145}{45}9
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(1, 0, 8),
      RemoveCandidates(2, 0, 8),
      RemoveCandidates(7, 2, 4)
    )
    assertLogicalSolution(expected, board, yWing)
  }

  test("test 2") {
    val board =
      """
        |65{379}{3478}{347}{34789}1{37}2
        |2{19}8{16}{37}{169}4{37}5
        |{13}4{137}52{137}896
        |{149}36{124}8{14}{29}57
        |{49}8{59}{2347}6{3457}{29}1{34}
        |72{15}{134}9{1345}68{34}
        |574912368
        |{39}{69}2{368}5{368}741
        |8{16}{13}{3467}{347}{3467}529
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(7, 1, 9),
      RemoveCandidates(7, 3, 6)
    )
    assertLogicalSolution(expected, board, yWing)
  }

  test("test 3") {
    val board =
      """
        |{35}{69}28{579}4{79}{3679}1
        |{35}{19}4{179}6{159}2{379}8
        |87{16}32{19}4{69}5
        |923618{57}{57}4
        |4{18}5{279}{79}{29}6{18}3
        |7{168}{16}543{18}29
        |258{19}37{19}46
        |649{12}8{125}3{15}7
        |1374{59}6{589}{589}2
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(0, 4, 9),
      RemoveCandidates(1, 7, 9)
    )
    assertLogicalSolution(expected, board, yWing)
  }

  test("test 4") {
    val board =
      """
        |{46}9172385{46}
        |7{346}{24}851{346}{269}{469}
        |{25}{35}8469{13}{12}7
        |{15}73248{16}{169}{569}
        |{125}8{25}396{147}{17}{45}
        |{46}{46}9175283
        |917684532
        |8{45}{45}932{67}{67}1
        |326517948
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(1, 8, 4),
      RemoveCandidates(3, 7, 1, 6),
      RemoveCandidates(4, 6, 7),
      RemoveCandidates(7, 6, 6),
      RemoveCandidates(7, 7, 7)
    )
    assertLogicalSolution(expected, board, yWing)
  }