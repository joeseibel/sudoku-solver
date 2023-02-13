package sudokusolver.scala.logic.simple

import munit.FunSuite
import sudokusolver.scala.RemoveCandidates
import sudokusolver.scala.logic.assertLogicalSolution

class NakedQuadsTest extends FunSuite:
  test("test") {
    val board =
      """
        |{15}{1245}{2457}{45}3{19}{79}86
        |{1568}{1568}{35678}{56}2{19}{79}4{13}
        |{16}9{346}{46}7852{13}
        |371856294
        |9{68}{68}142375
        |4{25}{25}397618
        |2{146}{46}7{16}3859
        |{18}392{18}5467
        |7{568}{568}9{68}4132
        |""".stripMargin.replace("\n", "")
    val expected = Seq(
      RemoveCandidates(0, 1, 1, 5),
      RemoveCandidates(0, 2, 5),
      RemoveCandidates(1, 2, 5, 6, 8),
      RemoveCandidates(2, 2, 6)
    )
    assertLogicalSolution(expected, board, nakedQuads)
  }