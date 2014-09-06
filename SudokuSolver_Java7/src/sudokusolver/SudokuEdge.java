package sudokusolver;

import org.jgrapht.graph.DefaultEdge;

public class SudokuEdge extends DefaultEdge {
	public enum LinkType {STRONG_LINK, WEAK_LINK}
	
	private LinkType linkType = null;
	
	public void setLinkType(LinkType linkType) {
		this.linkType = linkType;
	}
	
	public LinkType getLinkType() {
		return linkType;
	}
}