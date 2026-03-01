use crate::collections::IteratorZipExt;
use petgraph::{
    Undirected,
    algo::scc::tarjan_scc,
    dot::{Config, Dot},
    graphmap::NodeTrait,
    prelude::{GraphMap, UnGraphMap},
    visit::{
        self, DfsEvent, EdgeRef, GraphBase, GraphProp, IntoEdgeReferences, IntoEdges,
        IntoNeighbors, IntoNodeIdentifiers, IntoNodeReferences, NodeIndexable, Visitable,
    },
};
use std::{
    collections::{HashMap, HashSet},
    fmt::Debug,
    hash::Hash,
    ops::Index,
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

pub fn edge_attributes<E: EdgeRef<Weight = Strength>>(edge: E) -> String {
    match edge.weight() {
        Strength::Strong => String::new(),
        Strength::Weak => String::from("style = dashed"),
    }
}

// Continuously trims the graph of vertices that cannot be part of a cycle for X-Cycles rule 1. The modified graph will
// either be empty or only contain vertices with a degree of two or more and be connected by at least one strong link
// and one weak link.
pub fn trim<N: NodeTrait>(graph: &mut UnGraphMap<N, Strength>) {
    loop {
        let to_remove = graph.nodes().find(|&vertex| {
            let edges: Vec<_> = graph.edges(vertex).collect();
            edges.len() < 2
                || !edges
                    .iter()
                    .any(|&(_, _, &strength)| strength == Strength::Strong)
        });
        match to_remove {
            Some(to_remove) => graph.remove_node(to_remove),
            None => break,
        };
    }
}

pub fn get_weak_edges_in_alternating_cycle<
    'a,
    G: GraphProp<EdgeType = Undirected> + Index<G::EdgeId, Output = Strength>,
>(
    graph: &'a G,
) -> HashSet<G::EdgeId>
where
    G::NodeId: Eq + Hash,
    G::EdgeId: Eq + Hash,
    &'a G: IntoEdges<EdgeWeight = Strength> + GraphBase<EdgeId = G::EdgeId, NodeId = G::NodeId>,
{
    let mut weak_edges_in_alternating_cycle = HashSet::new();
    for edge in graph
        .edge_references()
        .filter(|edge| *edge.weight() == Strength::Weak)
    {
        if !weak_edges_in_alternating_cycle.contains(&edge.id()) {
            weak_edges_in_alternating_cycle.extend(get_alternating_cycle_weak_edges(graph, edge));
        }
    }
    weak_edges_in_alternating_cycle
}

fn get_alternating_cycle_weak_edges<
    'a,
    G: GraphProp<EdgeType = Undirected> + Index<G::EdgeId, Output = Strength>,
    E: EdgeRef<EdgeId = G::EdgeId, NodeId = G::NodeId, Weight = Strength>,
>(
    graph: &'a G,
    start_edge: E,
) -> Vec<G::EdgeId>
where
    G::NodeId: Eq + Hash,
    &'a G: IntoEdges<EdgeWeight = Strength> + GraphBase<EdgeId = G::EdgeId, NodeId = G::NodeId>,
{
    assert_eq!(
        *start_edge.weight(),
        Strength::Weak,
        "start_edge must be weak."
    );
    let start = start_edge.source();
    let end = start_edge.target();

    fn get_alternating_cycle_weak_edges<
        G: GraphProp<EdgeType = Undirected> + IntoEdges<EdgeWeight = Strength, NodeId: Eq + Hash>,
    >(
        graph: G,
        end: G::NodeId,
        current_vertex: G::NodeId,
        next_type: Strength,
        visited: HashSet<G::NodeId>,
        weak_edges: Vec<G::EdgeId>,
    ) -> Vec<G::EdgeId> {
        let next_edges_and_vertices: Vec<_> = graph
            .edges(current_vertex)
            .filter(|edge| edge.weight().is_compatible_with(next_type))
            .map(|edge| (edge, get_opposite_vertex(edge, current_vertex)))
            .collect();
        if next_type == Strength::Strong
            && next_edges_and_vertices
                .iter()
                .any(|&(_, next_vertex)| next_vertex == end)
        {
            weak_edges
        } else {
            next_edges_and_vertices
                .iter()
                .filter(|&&(_, next_vertex)| next_vertex != end && !visited.contains(&next_vertex))
                .map(|&(next_edge, next_vertex)| {
                    let mut next_visited = visited.clone();
                    next_visited.insert(next_vertex);
                    let mut next_weak_edges = weak_edges.clone();
                    if *next_edge.weight() == Strength::Weak {
                        next_weak_edges.push(next_edge.id());
                    }
                    get_alternating_cycle_weak_edges(
                        graph,
                        end,
                        next_vertex,
                        next_type.opposite(),
                        next_visited,
                        next_weak_edges,
                    )
                })
                .find(|next_result| !next_result.is_empty())
                .unwrap_or_default()
        }
    }

    let mut visited = HashSet::new();
    visited.insert(start);
    let weak_edges = get_alternating_cycle_weak_edges(
        graph,
        end,
        start,
        Strength::Strong,
        visited,
        vec![start_edge.id()],
    );
    assert!(
        !weak_edges
            .iter()
            .any(|&edge| graph[edge] == Strength::Strong)
    );
    weak_edges
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

pub fn to_dot<
    G: GraphProp
        + IntoNodeReferences<NodeWeight: Debug>
        + IntoEdgeReferences<EdgeWeight: Debug>
        + NodeIndexable,
>(
    graph: G,
    get_edge_attributes: impl Fn(G::EdgeRef) -> String,
    get_node_attributes: impl Fn(G::NodeRef) -> String,
) -> String {
    let edge_attributes = |_, edge| get_edge_attributes(edge);
    let node_attributes = |_, vertex| format!(r#"label = "{}""#, get_node_attributes(vertex));
    let dot = Dot::with_attr_getters(
        graph,
        &[Config::EdgeNoLabel, Config::NodeNoLabel],
        &edge_attributes,
        &node_attributes,
    );
    format!("{dot:?}")
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
