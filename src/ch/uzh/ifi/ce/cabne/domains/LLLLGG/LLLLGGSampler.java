package ch.uzh.ifi.ce.cabne.domains.LLLLGG;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public class LLLLGGSampler extends BidSampler<Double[], Double[]> {
	
	public LLLLGGSampler(BNESolverContext<Double[], Double[]> context) {
		super(context);
	}

	public Iterator<Sample> conditionalBidIterator(int i, Double[] v, Double[] b, List<Strategy<Double[], Double[]>> s) {	
		Iterator<double[]> rngiter = context.getRng(10).nextVectorIterator();
		
		// density is computed assuming uniform value distributions
		double densityTmp = 1.0;
		for (int j=0; j<6; j++) {
			if (j==i) continue;
			Double[] maxval = s.get(j).getMaxValue();
			densityTmp /= maxval[0] * maxval[1];
		}
		final double density = densityTmp;
		
		Iterator<Sample> it = new Iterator<Sample>() {
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Sample next() {
				double[] r;
				Double result[][] = new Double[6][];
				
				result[i] = b;
				
        		r = rngiter.next();
            
        		for (int j=0; j<6; j++) {
        			if (j==i) continue;
        			int offset = (j>i) ? -1 : 0;
        			double maxValue = (j<=3) ? 1.0 : 2.0;
        			Double[] valueJ = new Double[]{
            			r[2*(j+offset)] * maxValue,
            			r[2*(j+offset) + 1] * maxValue
            		};
        			result[j] = s.get(j).getBid(valueJ);
        		}
				return new Sample(density, result);
			}
		};
		return it;
	}
}
