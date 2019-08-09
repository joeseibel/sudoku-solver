package sudokusolver.kotlin.logic.tough

import org.jgrapht.alg.connectivity.BiconnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.traverse.BreadthFirstIterator
import sudokusolver.kotlin.Board
import sudokusolver.kotlin.Cell
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.mergeToRemoveCandidates
import sudokusolver.kotlin.zipEveryPair

/*
 * http://www.sudokuwiki.org/Singles_Chains
 *
 * A single's chain is a graph for a particular candidate that connects two cells when those are the only two cells in a
 * unit with that candidate. Each chain is colored with alternating colors such that for a given vertex with a given
 * color, all adjacent vertices have the opposite color. The two colors represent the two possible solutions for each
 * cell in the chain. Either the first color is the solution for the chain or the second color is.
 *
 * If there are two or more vertices with the same color that are in the same unit, then that color cannot be the
 * solution. All candidates with that color in that chain can be removed.
 *
 * For each candidate
 *   For each unit
 *     If the candidate appears in two unsolved cells of the unit
 *       Create an edge between the two cells
 *   For each connected component in the graph
 *     Traverse the connected component and assign alternating colors
 *     If the connected component contains at least two vertices that are in the same unit and have the same color
 *       Remove the candidate from vertices of the connected component that have that color
 */
fun simpleColoringRule2(board: Board<Cell>): List<RemoveCandidates> =
    SudokuNumber.values().flatMap { candidate ->
        board.units
            .map { unit -> unit.filterIsInstance<UnsolvedCell>().filter { candidate in it.candidates } }
            .filter { withCandidate -> withCandidate.size == 2 }
            .fold(GraphBuilder(SimpleGraph<UnsolvedCell, DefaultEdge>(DefaultEdge::class.java))) { builder, (a, b) ->
                builder.addEdge(a, b)
            }
            .buildAsUnmodifiable()
            .let { graph -> BiconnectivityInspector(graph).connectedComponents }
            .mapNotNull { subgraph ->
                val breadthFirst = BreadthFirstIterator(subgraph)
                val colors = breadthFirst.asSequence().associateWith { cell ->
                    if (breadthFirst.getDepth(cell) % 2 == 0) VertexColor.COLOR_ONE else VertexColor.COLOR_TWO
                }
                subgraph.vertexSet()
                    .toList()
                    .zipEveryPair()
                    .find { (a, b) ->
                        colors[a] == colors[b] && (a.row == b.row || a.column == b.column || a.block == b.block)
                    }
                    ?.let { (a, _) -> colors[a] }
                    ?.let { colorToRemove ->
                        subgraph.vertexSet().filter { colors[it] == colorToRemove }.map { it to candidate }
                    }
            }
            .flatten()
    }.mergeToRemoveCandidates()

private enum class VertexColor { COLOR_ONE, COLOR_TWO }