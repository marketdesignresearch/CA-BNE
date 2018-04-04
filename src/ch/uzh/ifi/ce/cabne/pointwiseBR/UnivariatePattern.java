package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.ArrayList;
import java.util.List;

public class UnivariatePattern extends Pattern<Double> {
	
	public UnivariatePattern() {
		super(1);
	}

	@Override
	List<Double> getPatternPoints(Double center, int npoints, double scale) {
		if (npoints < 3) throw new RuntimeException();
		if (npoints % 2 != 1) throw new RuntimeException();
		
		List<Double> result = new ArrayList<>(npoints);
		int halfsize = (npoints-1) / 2;
		for (int x=-halfsize; x<=halfsize; x++) {
			Double patternPoint = Math.max(center + x*scale/halfsize, 0.0);
			result.add(patternPoint);
		}
		return result;
	}

	@Override
	int getCenterIndex(int npoints) {
		return (npoints-1) / 2;
	}
	
	

}
