package sudokusolver.scala

import munit.FunSuite

class CellTest extends FunSuite:
  test("UnsolvedCell candidates are empty") {
    val message = "requirement failed: candidates must not be empty."
    interceptMessage[IllegalArgumentException](message)(UnsolvedCell(0, 0, Set.empty))
  }

  test("parseSimpleCells wrong length") {
    val message = "requirement failed: simpleBoard.length is 0, must be 81."
    interceptMessage[IllegalArgumentException](message)(parseSimpleCells(""))
  }

  test("parseCellsWithCandidates unmatched opening brace") {
    val message = "Unmatched '{'."
    interceptMessage[IllegalArgumentException](message)(parseCellsWithCandidates("{"))
  }

  test("parseCellsWithCandidates empty braces") {
    val message = "Empty \"{}\"."
    interceptMessage[IllegalArgumentException](message)(parseCellsWithCandidates("{}"))
  }

  test("parseCellsWithCandidates nested brace") {
    val message = "Nested '{'."
    interceptMessage[IllegalArgumentException](message)(parseCellsWithCandidates("{{}"))
  }

  test("parseCellsWithCandidates invalid character in braces") {
    val message = "ch is 'a', must be between '1' and '9'."
    interceptMessage[IllegalArgumentException](message)(parseCellsWithCandidates("{a}"))
  }

  test("parseCellsWithCandidates unmatched closing brace") {
    val message = "Unmatched '}'."
    interceptMessage[IllegalArgumentException](message)(parseCellsWithCandidates("}"))
  }

  test("parseCellsWithCandidates invalid character") {
    val message = "ch is 'a', must be between '1' and '9'."
    interceptMessage[IllegalArgumentException](message)(parseCellsWithCandidates("a"))
  }

  test("parseCellsWithCandidates wrong length") {
    val message = "requirement failed: Found 0 cells, required 81."
    interceptMessage[IllegalArgumentException](message)(parseCellsWithCandidates(""))
  }