package sudokusolver.javanostreams;

@SuppressWarnings("ClassCanBeRecord")
public class StrengthEdge {
    private final Strength strength;

    public StrengthEdge(Strength strength) {
        this.strength = strength;
    }

    public Strength getStrength() {
        return strength;
    }
}
