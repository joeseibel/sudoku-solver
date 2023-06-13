package sudokusolver.scala

import scalax.collection.generic.Edge
import scalax.collection.immutable.Graph

enum VertexColor:
  case COLOR_ONE, COLOR_TWO

extension[N, E <: Edge[N]] (graph: Graph[N, E])
  def colorToMap: Map[N, VertexColor] =
    graph.traverseWithDepth
      .map((vertex, depth) => (vertex, if depth % 2 == 0 then VertexColor.COLOR_ONE else VertexColor.COLOR_TWO))
      .toMap

  def colorToLists: (Seq[N], Seq[N]) =
    graph.traverseWithDepth.partitionMap((vertex, depth) => if depth % 2 == 0 then Left(vertex) else Right(vertex))

  private def traverseWithDepth: Seq[(N, Int)] =

    def processNode(node: N, depth: Int, visited: Map[N, Int]): Map[N, Int] =
      graph.get(node).neighbors.map(_.outer).foldLeft(visited + (node -> depth)) { (nextVisited, next) =>
        if nextVisited.contains(next) then nextVisited else nextVisited ++ processNode(next, depth + 1, nextVisited)
      }

    graph.nodes.headOption match
      case Some(startVertex) => processNode(startVertex.outer, 0, Map.empty).toSeq
      case None => Seq.empty