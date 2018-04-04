package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultivariateGaussianPattern extends Pattern<Double[]> {
	Random random;
	
	public MultivariateGaussianPattern(int d) {
		super(d);
		this.random = new Random();
	}

	@Override
	List<Double[]> getPatternPoints(Double[] center, int npoints, double scale) {
		List<Double[]> result = new ArrayList<>(npoints);
		result.add(center);
		
		for (int x=1; x<npoints; x++) {
			Double[] patternPoint = new Double[center.length];
			for (int d=0; d<dimension; d++) {
				patternPoint[d] = Math.max(random.nextGaussian() * scale + center[d], 0.0);
			}
			result.add(patternPoint);
		}
		
		return result;
	}

	@Override
	int getCenterIndex(int npoints) {
		return 0;
	}

}
