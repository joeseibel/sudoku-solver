use crate::collections::IteratorZipExt;
use petgraph::{
    Undirected,
    algo::scc::tarjan_scc,
    graphmap::NodeTrait,
    prelude::{GraphMap, UnGraphMap},
    visit::{
        self, DfsEvent, EdgeRef, GraphProp, IntoEdges, IntoNeighbors, IntoNodeIdentifiers,
        Visitable,
    },
};
use std::{
    collections::{HashMap, HashSet},
    hash::Hash,
};

#[derive(Clone, Copy, PartialEq)]
pub enum VertexColor {
    ColorOne,
    ColorTwo,
}

impl VertexColor {
    pub fn opposite(&self) -> Self {
        match self {
            Self::ColorOne => Self::ColorTwo,
            Self::ColorTwo => Self::ColorOne,
        }
    }
}

pub fn color_to_map<
    G: GraphProp<EdgeType = Undirected>
        + IntoNeighbors
        + IntoNodeIdentifiers<NodeId: Eq + Hash>
        + Visitable,
>(
    graph: G,
) -> HashMap<G::NodeId, VertexColor> {
    let mut colors = HashMap::new();
    if let start_vertex_option @ Some(start_vertex) = graph.node_identifiers().next() {
        colors.insert(start_vertex, VertexColor::ColorOne);
        visit::depth_first_search(graph, start_vertex_option, |event| {
            if let DfsEvent::TreeEdge(a, b) = event {
                colors.insert(b, colors[&a].opposite());
            }
        });
    }
    colors
}

pub fn color_to_lists<
    G: GraphProp<EdgeType = Undirected> + IntoNeighbors + IntoNodeIdentifiers + Visitable,
>(
    graph: G,
) -> (Vec<G::NodeId>, Vec<G::NodeId>) {
    let mut color_one = Vec::new();
    let mut color_two = Vec::new();
    if let start_vertex_option @ Some(start_vertex) = graph.node_identifiers().next() {
        color_one.push(start_vertex);
        visit::depth_first_search(graph, start_vertex_option, |event| {
            if let DfsEvent::TreeEdge(a, b) = event {
                if color_one.contains(&a) {
                    color_two.push(b);
                } else {
                    color_one.push(b);
                }
            }
        });
    }
    (color_one, color_two)
}

#[derive(Clone, Copy, Debug, Eq, Hash, PartialEq)]
pub enum Strength {
    Strong,
    Weak,
}

impl Strength {
    pub fn opposite(self) -> Self {
        match self {
            Self::Strong => Self::Weak,
            Self::Weak => Self::Strong,
        }
    }

    // For solutions that look for alternating edge types in a graph, it can sometimes be the case that a strong link
    // can take the place of a weak link. In those cases, this method should be called instead of performing an equality
    // check.
    pub fn is_compatible_with(self, required_type: Self) -> bool {
        match self {
            Self::Strong => true,
            Self::Weak => required_type == Self::Weak,
        }
    }
}

pub fn alternating_cycle_exists<
    G: GraphProp<EdgeType = Undirected>
        + IntoEdges<Edges: Clone, EdgeWeight = Strength, NodeId: Eq + Hash>,
>(
    graph: G,
    vertex: G::NodeId,
    adjacent_edges_type: Strength,
) -> bool {
    graph
        .edges(vertex)
        .filter(|edge| *edge.weight() == adjacent_edges_type)
        .zip_every_pair()
        .any(|(edge_a, edge_b)| {
            let start = get_opposite_vertex(edge_a, vertex);
            let end = get_opposite_vertex(edge_b, vertex);

            fn alternating_cycle_exists<
                G: GraphProp<EdgeType = Undirected>
                    + IntoEdges<EdgeWeight = Strength, NodeId: Eq + Hash>,
            >(
                graph: G,
                adjacent_edges_type: Strength,
                end: G::NodeId,
                current_vertex: G::NodeId,
                next_type: Strength,
                visisted: HashSet<G::NodeId>,
            ) -> bool {
                let mut next_vertices: HashSet<_> = graph
                    .edges(current_vertex)
                    .filter(|edge| edge.weight().is_compatible_with(next_type))
                    .map(|edge| get_opposite_vertex(edge, current_vertex))
                    .collect();
                if adjacent_edges_type.opposite() == next_type && next_vertices.contains(&end) {
                    true
                } else {
                    next_vertices.retain(|&vertex| !visisted.contains(&vertex) && vertex != end);
                    next_vertices.iter().any(|&next_vertex| {
                        let mut next_visited = visisted.clone();
                        next_visited.insert(next_vertex);
                        alternating_cycle_exists(
                            graph,
                            adjacent_edges_type,
                            end,
                            next_vertex,
                            next_type.opposite(),
                            next_visited,
                        )
                    })
                }
            }

            let mut visited = HashSet::new();
            visited.insert(vertex);
            visited.insert(start);
            alternating_cycle_exists(
                graph,
                adjacent_edges_type,
                end,
                start,
                adjacent_edges_type.opposite(),
                visited,
            )
        })
}

pub fn connected_components<N: NodeTrait + PartialEq>(
    graph: &UnGraphMap<N, ()>,
) -> Vec<UnGraphMap<N, ()>> {
    // If I don't annotate the type of components, then I get an error later stating that the type of subgraph in the
    // for loop can't be inferred. I suspect this is a bug in the compiler's type inference algorithm. It would be good
    // for me to create a simple example and file a bug report.
    let components: Vec<_> = tarjan_scc::tarjan_scc(graph)
        .iter()
        .map(|graph_vertices| {
            let edges = graph_vertices
                .iter()
                .zip_every_pair()
                .filter(|&(&a, &b)| graph.contains_edge(a, b))
                .map(|(&a, &b)| (a, b));
            GraphMap::from_edges(edges)
        })
        .collect();
    let mut vertex_count = 0;
    let mut edge_count = 0;
    for subgraph in &components {
        vertex_count += subgraph.node_count();
        edge_count += subgraph.edge_count();
    }
    assert_eq!(graph.node_count(), vertex_count);
    assert_eq!(graph.edge_count(), edge_count);
    components
}

pub fn get_opposite_vertex<E: EdgeRef<NodeId: PartialEq>>(edge: E, vertex: E::NodeId) -> E::NodeId {
    let source = edge.source();
    let target = edge.target();
    if vertex == source {
        target
    } else if vertex == target {
        source
    } else {
        panic!("vertex must be an endpoint of edge.");
    }
}
