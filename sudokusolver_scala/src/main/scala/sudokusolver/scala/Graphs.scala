package sudokusolver.scala

import scalax.collection.generic.{AbstractGenericUnDiEdge, Edge}
import scalax.collection.immutable.Graph
import scalax.collection.io.dot.implicits.{toId, toNodeId}
import scalax.collection.io.dot.{DotAttr, DotEdgeStmt}

import scala.annotation.tailrec

enum VertexColor:
  def opposite: VertexColor = this match
    case COLOR_ONE => COLOR_TWO
    case COLOR_TWO => COLOR_ONE

  case COLOR_ONE, COLOR_TWO

extension[N, E <: Edge[N]] (graph: Graph[N, E])
  def colorToMap: Map[N, VertexColor] =
    graph.traverseWithDepth
      .map((vertex, depth) => vertex -> (if depth % 2 == 0 then VertexColor.COLOR_ONE else VertexColor.COLOR_TWO))
      .toMap

  def colorToLists: (Seq[N], Seq[N]) =
    graph.traverseWithDepth.partitionMap((vertex, depth) => if depth % 2 == 0 then Left(vertex) else Right(vertex))

  private def traverseWithDepth: Seq[(N, Int)] =

    def processNode(node: N, depth: Int, visited: Map[N, Int]): Map[N, Int] =
      graph.get(node).neighbors.foldLeft(visited + (node -> depth)) { (nextVisited, next) =>
        if nextVisited.contains(next) then nextVisited else nextVisited ++ processNode(next, depth + 1, nextVisited)
      }

    graph.nodes.headOption match
      case Some(startVertex) => processNode(startVertex, 0, Map.empty).toSeq
      case None => Nil

class StrengthEdge[+N](val source: N, val target: N, val strength: Strength)
  extends AbstractGenericUnDiEdge[N, StrengthEdge]:

  def map[NN](node_1: NN, node_2: NN): StrengthEdge[NN] = StrengthEdge(node_1, node_2, strength)

  def toDotEdgeStmt(getVertexLabel: N => String): DotEdgeStmt =
    val sourceLabel = getVertexLabel(source)
    val targetLabel = getVertexLabel(target)
    strength match
      case Strength.STRONG => DotEdgeStmt(sourceLabel, targetLabel)
      case Strength.WEAK => DotEdgeStmt(sourceLabel, targetLabel, Seq(DotAttr("style", "dashed")))

enum Strength:
  def opposite: Strength = this match
    case STRONG => WEAK
    case WEAK => STRONG

  def isCompatibleWith(requiredType: Strength): Boolean = this match
    case STRONG => true
    case WEAK => requiredType == WEAK

  case STRONG, WEAK

extension[N] (graph: Graph[N, StrengthEdge[N]])
  /*
   * Continuously trims the graph of vertices that cannot be part of a cycle for X-Cycles rule 1. The returned graph
   * will either be empty or only contain vertices with a degree of two or more and be connected by at least one strong
   * link and one weak link.
   */
  @tailrec
  def trim: Graph[N, StrengthEdge[N]] =
    val toRemove = graph.nodes.filter { vertex =>
      val edges = vertex.edges
      edges.size < 2 || edges.forall(_.outer.strength == Strength.WEAK)
    }
    if toRemove.isEmpty then graph else graph.removedAll(toRemove.map(_.outer), Nil).trim

def getWeakEdgesInAlternatingCycle[N](graph: Graph[N, StrengthEdge[N]]): Set[StrengthEdge[N]] =
  graph.edges
    .filter(_.strength == Strength.WEAK)
    .foldLeft(Set.empty[StrengthEdge[N]]) { (weakEdgesInAlternatingCycle, edge) =>
      if weakEdgesInAlternatingCycle.contains(edge) then
        weakEdgesInAlternatingCycle
      else
        weakEdgesInAlternatingCycle ++ getAlternatingCycleWeakEdges(graph, edge)
    }

private def getAlternatingCycleWeakEdges[N](
                                             graph: Graph[N, StrengthEdge[N]],
                                             startEdge: StrengthEdge[N]
                                           ): Seq[StrengthEdge[N]] =
  require(startEdge.strength == Strength.WEAK, "startEdge must be weak.")
  val start = startEdge.source
  val end = startEdge.target

  def getAlternatingCycleWeakEdges(
                                    currentVertex: N,
                                    nextType: Strength,
                                    visited: Set[N],
                                    weakEdges: Seq[StrengthEdge[N]]
                                  ): Seq[StrengthEdge[N]] =
    val nextEdgesAndVertices = for
      edge <- graph.get(currentVertex).edges
      if edge.strength.isCompatibleWith(nextType)
    yield edge -> edge.outer.getOppositeVertex(currentVertex)
    if nextType == Strength.STRONG && nextEdgesAndVertices.exists((_, nextVertex) => nextVertex == end) then
      weakEdges
    else
      val nextWeakEdges = nextEdgesAndVertices.to(LazyList)
        .filter((_, nextVertex) => nextVertex != end && !visited.contains(nextVertex))
        .map { (nextEdge, nextVertex) =>
          getAlternatingCycleWeakEdges(
            nextVertex,
            nextType.opposite,
            visited + nextVertex,
            if nextEdge.strength == Strength.WEAK then weakEdges :+ nextEdge else weakEdges
          )
        }
        .find(_.nonEmpty)
      nextWeakEdges match
        case Some(nextWeakEdges) => nextWeakEdges
        case None => Nil

  val weakEdges = getAlternatingCycleWeakEdges(start, Strength.STRONG, Set(start), List(startEdge))
  assert(weakEdges.forall(_.strength == Strength.WEAK), "There are strong edges in the return value.")
  weakEdges

def alternatingCycleExists[N](graph: Graph[N, StrengthEdge[N]], vertex: N, adjacentEdgesType: Strength): Boolean =
  graph.get(vertex).edges.filter(_.strength == adjacentEdgesType).toIndexedSeq.zipEveryPair.exists { (edgeA, edgeB) =>
    val start = edgeA.outer.getOppositeVertex(vertex)
    val end = edgeB.outer.getOppositeVertex(vertex)

    def alternatingCycleExists(currentVertex: N, nextType: Strength, visited: Set[N]): Boolean =
      val nextVertices = for
        edge <- graph.get(currentVertex).edges
        if edge.strength.isCompatibleWith(nextType)
      yield edge.outer.getOppositeVertex(currentVertex)
      adjacentEdgesType.opposite == nextType && nextVertices.contains(end) ||
        (nextVertices &~ visited - end).exists { nextVertex =>
          alternatingCycleExists(nextVertex, nextType.opposite, visited + nextVertex)
        }

    alternatingCycleExists(start, adjacentEdgesType.opposite, Set(vertex, start))
  }

extension[N] (edge: Edge[N])
  private def getOppositeVertex(vertex: N): N =
    val source = edge._1
    val target = edge._2
    vertex match
      case `source` => target
      case `target` => source
      case _ => throw IllegalArgumentException("vertex must be an endpoint of edge.")
