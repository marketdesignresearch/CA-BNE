package ch.uzh.ifi.ce.cabne.strategy;

import java.util.SortedMap;
import java.util.TreeMap;

// single-dimensional piecewise linear strategy

public class UnivariatePWLStrategy implements Strategy<Double, Double> {
	double[] values, bids;
	SortedMap<Double, Double> data;
	int n;
	boolean isAscending;
	double maxValue;
	
	public UnivariatePWLStrategy(SortedMap<Double, Double> intervals) {
		// don't use a TreeMap or anything similar for looking up the intervals.
		// After construction, we prefer a fast static data structure, i.e. a good old sorted array.
		// the map used to initialize is kept around so it can be recovered
		data = intervals;
		
		n = intervals.size();
		values = new double[n+2];
		bids = new double[n+2];
		
		int i = 0;
		for (double key : intervals.keySet()) {
			i++;
			values[i] = key;
			bids[i] = intervals.get(key);
		}
		values[0] = -1.0;
		values[n+1] = Double.MAX_VALUE; // TODO: test that this interpolates correctly. If not, need to do something about it
		bids[0] = bids[1];
		bids[n+1] = bids[n];
		
		isAscending = true;
		for (i=0; i<n+1; i++) {
			if (bids[i+1] < bids[i]) {
				isAscending = false;
				break;
			}
		}
		
		maxValue = values[n];
	}
	
	public static UnivariatePWLStrategy makeTruthful(double lower, double upper) {
		SortedMap<Double, Double> intervals = new TreeMap<>();
		intervals.put(lower, lower);
		intervals.put(upper, upper);
		return new UnivariatePWLStrategy(intervals);		
	}
	
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
				
		double floor = values[lo];
		double ceiling = values[hi];
		
		// TODO this shouldn't be needed or should it?
		if (n==2) return value;
        
        double weight = (value - floor) / (ceiling - floor);
        //return weight * bids[hi] + (1 - weight) * bids[lo];
        return bids[lo] + weight * (bids[hi] - bids[lo]); // same as above line but faster?
	}
	
	public Double invert(Double bid) {
		if (!isAscending) {
			throw new RuntimeException("Can't invert nonmonotonic strategy.");
		}
		
		// binary search
		int lo = 0, hi=n+1;
		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (bids[middle] <= bid) {
				lo = middle;
			} else {
				hi = middle;
			}
		}
				
		double floor = values[lo];
		double ceiling = values[hi];
		
        double weight = (bid - floor) / (ceiling - floor);
        return weight * values[hi] + (1 - weight) * values[lo];        
	}
	
	@Override
    public String toString() {
        return "PiecewiseLinearStrategy{" +
                "values=" + values + " bids=" + bids +
                '}';
    }

	public SortedMap<Double, Double> getData() {
		return data;
	}

	@Override
	public Double getMaxValue() {
		return maxValue;
	}
}
