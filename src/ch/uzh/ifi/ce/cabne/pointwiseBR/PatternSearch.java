package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;


public class PatternSearch<Value, Bid> extends Optimizer<Value, Bid> {
	protected Pattern<Bid> pattern;
	double initialScale;
	
	public PatternSearch(BNESolverContext<Value, Bid> context, Pattern<Bid> pattern) {
		super(context);
		this.pattern = pattern;
		initialScale = 1.0;
	}


	public double getInitialScale() {
		return initialScale;
	}


	public void setInitialScale(double initialScale) {
		this.initialScale = initialScale;
	}


	// this method assumes that we are optimizing over [0, \infty).
	@Override
	public Result<Bid> findBR(int i, Value v, Bid currentBid, List<Strategy<Value, Bid>> strats) {
		int patternSize = context.getIntParameter("patternsearch.size");
		double patternscale = context.getDoubleParameter("patternsearch.stepsize") * initialScale;
		int nSteps = context.getIntParameter("patternsearch.nsteps");
		
        double[] fxx = new double[patternSize];
        Bid bestbid = currentBid;
        
        // cache the expected utilities to save on computation.
        Map<String, Double> cache = new Hashtable<>();

        for (int iter=0; iter<nSteps; iter++) {
        	List<Bid> patternPoints = pattern.getPatternPoints(bestbid, patternSize, patternscale);
        	for (int j=0; j<patternSize; j++) {
        		Bid bid = patternPoints.get(j);

        		if (cache.containsKey(pattern.bidHash(bid))) {
        			fxx[j] = cache.get(pattern.bidHash(bid));
            	} else {
            		double util = context.integrator.computeExpectedUtility(i, v, bid, strats);
            		cache.put(pattern.bidHash(bid), util);
            		fxx[j] = util;
            	}
            }
            
            int bestIndex = 0;
            for (int j=0; j<patternSize; j++) {
            	if (fxx[j] > fxx[bestIndex]) {
            		bestIndex = j;
            	}
            }
            if (bestIndex == pattern.getCenterIndex(patternSize)) {
        		// hit the center, decrease step size
        		patternscale *= 0.5;
        	} else {
        		// give less iterations as budget if the pattern is moving (each moving step consumes 2 iterations)
        		iter++;
        	}
            bestbid = patternPoints.get(bestIndex);
        }
        
        return new Result<>(bestbid, cache.get(pattern.bidHash(bestbid)), cache.get(pattern.bidHash(currentBid)));
	}
}
