package sudokusolver;

public enum VertexColor {
	BLACK, BLUE;
	
	public VertexColor getOpposite() {
		switch (this) {
			case BLACK:
				return BLUE;
			case BLUE:
				return BLACK;
			default:
				throw new AssertionError("CellColor problem");
		}
	}
}