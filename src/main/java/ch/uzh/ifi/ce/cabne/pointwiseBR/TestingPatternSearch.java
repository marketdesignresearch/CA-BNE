package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.statisticaltests.NewCAGatherer;
import ch.uzh.ifi.ce.cabne.statisticaltests.NonCombiningCachingSampleGatherer;
import ch.uzh.ifi.ce.cabne.statisticaltests.PairedAndUnpairedTTest;
import ch.uzh.ifi.ce.cabne.statisticaltests.TestFeedback;
import ch.uzh.ifi.ce.cabne.statisticaltests.TestStatus;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;


public class TestingPatternSearch<Value, Bid> extends PatternSearch<Value, Bid> {
	private final PairedAndUnpairedTTest tTest = new PairedAndUnpairedTTest();
	
	public TestingPatternSearch(BNESolverContext<Value, Bid> context, Pattern<Bid> pattern) {
		super(context, pattern);
	}

	// this method assumes that we are optimizing over [0, \infty).
	@SuppressWarnings("unchecked")
	@Override
	public Result<Bid> findBR(int i, Value v, Bid currentBid, List<Strategy<Value, Bid>> strats) {
		int patternSize = context.getIntParameter("patternsearch.size");
		double patternScale = context.getDoubleParameter("patternsearch.stepsize") * initialScale;
		int nSteps = context.getIntParameter("patternsearch.nsteps");
		
		int minsamples = Integer.parseInt(context.config.get("ttest.minsamples"));
		int maxsamples = Integer.parseInt(context.config.get("ttest.maxsamples"));
		double significance = Double.parseDouble(context.config.get("ttest.significance"));
        
        Bid bestbid = currentBid;
                
        List<NonCombiningCachingSampleGatherer> gatherers = new ArrayList<>(patternSize);
        Map<String, NewCAGatherer<Value, Bid>> gatherercache = new Hashtable<>();
        
        // initialize arrays
        for (int j=0; j<patternSize; j++) {
        	gatherers.add(null);
        }
		
        for (int iter=0; iter<nSteps; iter++) {

        	List<Bid> patternPoints = pattern.getPatternPoints(bestbid, patternSize, patternScale);
        	
        	// initialize gatherers, recycle from previous pattern steps if possible
        	for (int j=0; j<patternSize; j++) {
            	Bid bid = patternPoints.get(j);
            	if (gatherercache.containsKey(pattern.bidHash(bid))) {
            		gatherers.set(j, gatherercache.get(pattern.bidHash(bid)));
            	} else {
            		NewCAGatherer<Value, Bid> g = new NewCAGatherer<>(context, i, v, bid, strats);
            		g.computeMoreSamples(minsamples);
            		gatherercache.put(pattern.bidHash(bid), g);
            		gatherers.set(j, g);
            	}
            }
        	    	
            // add samples to all gatherers until we pass the t-test
            TestFeedback feedback;
            while (true) {
            	feedback = tTest.test(significance, gatherers, maxsamples, 1);
                
                if (feedback.getStatus() != TestStatus.UNDETERMINED) {
                	break;
                }
                
                for (int j=0; j < patternSize; ++j) {
                    int additionalSamples = feedback.getNextSamples()[j];
                	NewCAGatherer<Value, Bid> gatherer = (NewCAGatherer<Value, Bid>) gatherers.get(j);
                	gatherer.computeMoreSamples(additionalSamples);
                }
            }
            
            int bestIndex = feedback.getMaxIndices().first(); 
            if (feedback.getStatus() == TestStatus.FAILED) {
            	// decrease pattern size, apply an iteration penalty, but still return the best index we found,
            	// even if it's not statistically significant (otherwise we might get stuck here without ever moving away)
        		patternScale *= 0.5;
        		//iter += 1.0;
            } else if (bestIndex == pattern.getCenterIndex(patternSize)) {
            		// hit the center, decrease step size
            		patternScale *= 0.5;
            } else {
            		// give less iterations as budget if the pattern is moving (each moving step consumes 2 iterations)
            		iter += 1.0;
            }
                        
            bestbid = patternPoints.get(bestIndex);
        }
        
		return new Result<>(bestbid, gatherercache.get(pattern.bidHash(bestbid)).getMean(), gatherercache.get(pattern.bidHash(currentBid)).getMean());
	}
}
