package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.ArrayList;
import java.util.List;

public class MultivariateCrossPattern extends Pattern<Double[]> {
	
	public MultivariateCrossPattern(int d) {
		super(d);
	}

	@Override
	List<Double[]> getPatternPoints(Double[] center, int npoints, double scale) {
		// number of points that are accepted:
		//   1d: 3, 5, 7, ...
		// 	 2d: 5, 9, 13, ...
		//   3d: 7, 13, 19, ...
		if (npoints < 1 + 2*dimension) throw new RuntimeException();
		if (npoints % (2*dimension) != 1) throw new RuntimeException();
		
		List<Double[]> result = new ArrayList<>(npoints);
		result.add(center);
		int halfsize = (npoints-1) / (2*dimension); // number of pattern points on one arm of the cross
		for (int x=-halfsize; x<=halfsize; x++) {
			if (x==0) continue;
			for (int d=0; d<dimension; d++) {
				Double[] patternPoint = center.clone();
				patternPoint[d] = Math.max(patternPoint[d] + x*scale/halfsize, 0.0);
				result.add(patternPoint);
			}
		}
		
		return result;
	}

	@Override
	int getCenterIndex(int npoints) {
		return 0;
	}
	
	@Override
	protected String bidHash(Double[] key) {
		StringBuilder builder = new StringBuilder();
		for (int x=0; x<key.length; x++) {
			builder.append(String.format("%9.6f|", key[x]));
		}
		return builder.toString();
	}

}
