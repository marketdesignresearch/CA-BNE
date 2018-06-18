package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.List;

public abstract class Pattern<Bid> {
	int dimension;
	
	public Pattern(int d) {
		this.dimension = d;
	}
	
	abstract List<Bid> getPatternPoints(Bid center, int npoints, double scale);
	
	abstract int getCenterIndex(int npoints);
	
	String bidHash(Bid b) {
		return b.toString();
	}
	
}
