package ch.uzh.ifi.ce.cabne.statisticaltests;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.domains.Mechanism;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

// This gatherer holds an iterator yielding bids, which allows it to draw samples while keeping an internal 
// iterator.
// This allows for common random numbers to work with statistical testing

// This class can easily be adjusted to extend a CombiningSampleGatherer and inherit its properties

public class NewCAGatherer<Value, Bid> extends NonCombiningCachingSampleGatherer {
	Iterator<BidSampler<Value, Bid>.Sample> biditer;
	BNESolverContext<Value, Bid> context;
	Value v;
	int i;
	
	public NewCAGatherer(BNESolverContext<Value, Bid> context, int i, Value v, Bid b, List<Strategy<Value, Bid>> s) {
		super();
		this.context = context;
		this.biditer = context.sampler.conditionalBidIterator(i, v, b, s);
		this.v = v;
		this.i = i;
	}
	
	public void computeMoreSamples(int additionalSamples) {
		// add samples until gatherer has at least requiredSamples many samples.
		BidSampler<Value, Bid>.Sample sample;
		Mechanism<Value, Bid> mechanism = context.mechanism;
		
		for (int j=0; j<additionalSamples; j++) {
			sample = biditer.next();
			addValue(sample.density * mechanism.computeUtility(i, v, sample.bids));
		}
	}
	
}
