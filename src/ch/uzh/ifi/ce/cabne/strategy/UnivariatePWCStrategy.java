package ch.uzh.ifi.ce.cabne.strategy;

import java.util.SortedMap;

// single-dimensional piecewise constant strategy

public class UnivariatePWCStrategy extends UnivariatePWLStrategy {
	
	public UnivariatePWCStrategy(SortedMap<Double, Double> intervals) {
		super(intervals);
	}

	@Override
	public Double getBid(Double value) {
		// binary search
		int lo = 0, hi=n+1;
		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (values[middle] <= value) {
				lo = middle;
			} else {
				hi = middle;
			}
		}
				
        return bids[lo];
	}
}
