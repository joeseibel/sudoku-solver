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

pub fn get_opposite_vertex<N: PartialEq, E>(edge: (N, N, E), vertex: N) -> N {
    let (source, target, _) = edge;
    if vertex == source {
        target
    } else if vertex == target {
        source
    } else {
        panic!("vertex must be an endpoint of edge.")
    }
}
