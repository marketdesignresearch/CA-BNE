package ch.uzh.ifi.ce.cabne.domains;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public abstract class BidSampler<Value, Bid> {
	public class Sample {
		public double density;
		public Bid[] bids;
		
		public Sample(double density, Bid[] bids) {
			this.density = density;
			this.bids = bids;
		}
	};
	
	public BNESolverContext<Value, Bid> context;
	
	public BidSampler(BNESolverContext<Value, Bid> context) {
		this.context = context;
	}
	
	public void setContext(BNESolverContext<Value, Bid> context) {
		this.context = context;
	}
	
	// given i and v_i, returns an iterator that generates samples from s(v)
	// For this we need to draw a sample from v_{-i}.
	// v_i is needed as input because correlations are possible.
	public abstract Iterator<Sample> conditionalBidIterator(int i, Value v, Bid b, List<Strategy<Value, Bid>> s);
}
