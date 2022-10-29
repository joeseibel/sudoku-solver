package sudokusolver.kotlin.logic.diabolical

import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.BiconnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.LocatedCandidate
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.colorToLists
import sudokusolver.kotlin.colorToMap
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair

/*
 * https://www.sudokuwiki.org/3D_Medusa
 *
 * A 3D Medusa is a graph type in which each vertex is a particular candidate in a cell and each edge is a strong link.
 * A strong link is an edge such that if one vertex of the link is the solution, then the other vertex must not be the
 * solution. A strong link also means that if one vertex of the link is not the solution, then the other vertex must be
 * the solution. When a candidate is in only two cells of a unit, there is an edge between the candidate of those two
 * cells. Additionally, when a cell contains only two candidates, there is an edge between the two candidates of that
 * cell. Each medusa is colored with alternating colors such that for a given vertex with a given color, all adjacent
 * vertices have the opposite color. The two colors represent the two possible solutions. Either the first color is the
 * solution for the medusa or the second color is.
 *
 * Rule 1: Twice in a Cell
 *
 * If there are two vertices with the same color that are in the same cell, then that color cannot be the solution and
 * the opposite color must be the solution. All vertices with the opposite color can be set as the solution.
 */
fun medusaRule1(board: Board<Cell>): List<SetValue> =
    createConnectedComponents(board).mapNotNull { graph ->
        val colors = graph.colorToMap()
        graph.vertexSet()
            .toList()
            .zipEveryPair()
            .find { (a, b) ->
                val (cellA, _) = a
                val (cellB, _) = b
                cellA == cellB && colors[a] == colors[b]
            }
            ?.let { (a, _) -> colors[a] }
            ?.opposite
            ?.let { colorToSet ->
                graph.vertexSet()
                    .filter { colors[it] == colorToSet }
                    .map { (cell, candidate) -> SetValue(cell, candidate) }
            }
    }.flatten()

/*
 * Rule 2: Twice in a Unit
 *
 * If there are two vertices with the same color and the same candidate that are in the same unit, then that color
 * cannot be the solution and the opposite color must be the solution. All vertices with the opposite color can be set
 * as the solution.
 */
fun medusaRule2(board: Board<Cell>): List<SetValue> =
    createConnectedComponents(board).mapNotNull { graph ->
        val colors = graph.colorToMap()
        graph.vertexSet()
            .toList()
            .zipEveryPair()
            .find { (a, b) ->
                val (cellA, candidateA) = a
                val (cellB, candidateB) = b
                candidateA == candidateB && colors[a] == colors[b] && cellA isInSameUnit cellB
            }
            ?.let { (a, _) -> colors[a] }
            ?.opposite
            ?.let { colorToSet ->
                graph.vertexSet()
                    .filter { colors[it] == colorToSet }
                    .map { (cell, candidate) -> SetValue(cell, candidate) }
            }
    }.flatten()

/*
 * Rule 3: Two colors in a cell
 *
 * If there are two differently colored candidates in a cell, then the solution must be one of the two candidates. All
 * other candidates in the cell can be removed.
 */
fun medusaRule3(board: Board<Cell>): List<RemoveCandidates> =
    createConnectedComponents(board).mapNotNull { graph ->
        val colors = graph.colorToMap()
        graph.vertexSet()
            .filter { (cell, _) -> cell.candidates.size > 2 }
            .zipEveryPair()
            .find { (a, b) ->
                val (cellA, _) = a
                val (cellB, _) = b
                cellA == cellB && colors[a] != colors[b]
            }
            ?.let { (a, _) -> a }
            ?.let { (cell, _) -> cell }
            ?.let { cell ->
                cell.candidates
                    .map { cell to it }
                    .filter { it !in graph.vertexSet() }
            }
    }.flatten().mergeToRemoveCandidates()

/*
 * Rule 4: Two colors 'elsewhere'
 *
 * Given a candidate, if there is an unsolved cell with that candidate, it is uncolored, and the cell can see two other
 * cells which both have that candidate, and they are differently colored, then the candidate must be the solution to
 * one of the other cells, and it cannot be the solution to the first cell with the uncolored candidate. The uncolored
 * candidate can be removed from the first cell.
 */
fun medusaRule4(board: Board<Cell>): List<RemoveCandidates> =
    createConnectedComponents(board).flatMap { graph ->
        val (colorOne, colorTwo) = graph.colorToLists()
        board.cells
            .filterIsInstance<UnsolvedCell>()
            .flatMap { cell -> cell.candidates.map { candidate -> cell to candidate } }
            .filter { it !in graph.vertexSet() }
            .filter { (cell, candidate) ->

                fun canSeeColor(color: List<LocatedCandidate>) =
                    color.any { (coloredCell, coloredCandidate) ->
                        candidate == coloredCandidate && cell isInSameUnit coloredCell
                    }

                canSeeColor(colorOne) && canSeeColor(colorTwo)
            }
    }.mergeToRemoveCandidates()

/*
 * Rule 5: Two colors Unit + Cell
 *
 * If there is an unsolved cell with an uncolored candidate, that candidate can see a colored candidate of the same
 * number, and the unsolved cell contains a candidate colored with the opposite color, then either the candidate in the
 * same unit is the solution for that cell or the candidate in the same cell is the solution. In either case, the
 * uncolored candidate cannot be the solution and can be removed from the unsolved cell.
 */
fun medusaRule5(board: Board<Cell>): List<RemoveCandidates> =
    createConnectedComponents(board).flatMap { graph ->
        val (colorOne, colorTwo) = graph.colorToLists()
        board.cells
            .filterIsInstance<UnsolvedCell>()
            .flatMap { cell -> cell.candidates.map { candidate -> cell to candidate } }
            .filter { it !in graph.vertexSet() }
            .filter { (cell, candidate) ->

                fun canSeeColor(color: List<LocatedCandidate>) =
                    color.any { (coloredCell, coloredCandidate) ->
                        candidate == coloredCandidate && cell isInSameUnit coloredCell
                    }

                fun colorInCell(color: List<LocatedCandidate>) = cell.candidates.any { cell to it in color }

                canSeeColor(colorOne) && colorInCell(colorTwo) || canSeeColor(colorTwo) && colorInCell(colorOne)
            }
    }.mergeToRemoveCandidates()

/*
 * Rule 6: Cell Emptied by Color
 *
 * If there is an unsolved cell in which every candidate is uncolored and every candidate can see the same color, then
 * that color cannot be the solution since it would lead to the cell being emptied of candidates and still have no
 * solution. All vertices with the opposite color can be set as the solution.
 */
fun medusaRule6(board: Board<Cell>): List<SetValue> =
    createConnectedComponents(board).flatMap { graph ->
        val (colorOne, colorTwo) = graph.colorToLists()
        board.cells
            .filterIsInstance<UnsolvedCell>()
            .filter { cell -> cell.candidates.none { candidate -> cell to candidate in graph.vertexSet() } }
            .firstNotNullOfOrNull { cell ->

                fun everyCandidateCanSeeColor(color: List<LocatedCandidate>) =
                    cell.candidates.all { candidate ->
                        color.any { (coloredCell, coloredCandidate) ->
                            candidate == coloredCandidate && cell isInSameUnit coloredCell
                        }
                    }

                when {
                    everyCandidateCanSeeColor(colorOne) -> colorTwo
                    everyCandidateCanSeeColor(colorTwo) -> colorOne
                    else -> null
                }
            }
            ?.map { (coloredCell, coloredCandidate) -> SetValue(coloredCell, coloredCandidate) }
            ?: emptyList()
    }

private fun createConnectedComponents(board: Board<Cell>): Set<Graph<LocatedCandidate, DefaultEdge>> {
    val builder = GraphBuilder(SimpleGraph<LocatedCandidate, DefaultEdge>(DefaultEdge::class.java))
    board.cells.filterIsInstance<UnsolvedCell>().filter { it.candidates.size == 2 }.forEach { cell ->
        builder.addEdge(cell to cell.candidates.first(), cell to cell.candidates.last())
    }
    board.units.map { it.filterIsInstance<UnsolvedCell>() }.forEach { unit ->
        SudokuNumber.values().forEach { candidate ->
            unit.filter { candidate in it.candidates }
                .takeIf { it.size == 2 }
                ?.let { builder.addEdge(it.first() to candidate, it.last() to candidate) }
        }
    }
    return BiconnectivityInspector(builder.buildAsUnmodifiable()).connectedComponents
}