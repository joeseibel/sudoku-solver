import SwiftGraph

extension Edge {
    func getOppositeIndex(index: Int) -> Int {
        if index == u {
            v
        } else if index == v {
            u
        } else {
            preconditionFailure("Index not found in edge: \(index)")
        }
    }
}

extension Graph where E == WeightedEdge<Strength> {
    func toDOT(graphId: String? = nil, vertexLabelProvider getVertexLabel: (V) -> String) -> String {
        var result = "strict graph "
        if let graphId {
            result += graphId + " "
        }
        result += "{\n"
        for edge in edgeList() {
            let u = vertexAtIndex(edge.u)
            let v = vertexAtIndex(edge.v)
            result += "  \"\(getVertexLabel(u))\" -- \"\(getVertexLabel(v))\""
            if edge.weight == .weak {
                result += " [style = dashed]"
            }
            result += "\n"
        }
        result += "}"
        return result
    }
}

/*
 * Strength should be used as the weight for a WeightedUniqueElementsGraph.
 *
 * I considered having separate Strength and StrengthEdge types like I do for other languages. However, SwiftGraph
 * permits the weight to an arbitrary type as long as it is Equatable and Codable. This is different from JGraphT which
 * forces the weight to be a double.
 *
 * I am aware that this might be considered an abuse of the term "weight" since Strength is not a number. However, using
 * a weight makes the code simplier due to the methods that are available on Graph when its edge type is a WeightedEdge.
 * While using Strength as a weight might violate the mathematical concept of a weak, it doesn't break anything
 * regarding SwiftGraph.
 */
enum Strength: Codable {
    case strong, weak
    
    var opposite: Strength {
        switch self {
        case .strong:
            .weak
        case .weak:
            .strong
        }
    }
    
    /*
     * For solutions that look for alternating edge types in a graph, it can sometimes be the case that a strong link
     * can take the place of a weak link. In those cases, this method should be called instead of performing an equality
     * check.
     */
    func isCompatible(with requiredType: Strength) -> Bool {
        switch self {
        case .strong:
            true
        case .weak:
            requiredType == .weak
        }
    }
}

extension WeightedEdge<Strength>: Hashable {
    public func hash(into hasher: inout Hasher) {
        hasher.combine(u)
        hasher.combine(v)
        hasher.combine(directed)
        hasher.combine(weight)
    }
}
