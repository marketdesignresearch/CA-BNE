package ch.uzh.ifi.ce.cabne.integration;

import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public abstract class Integrator<Value, Bid> {
	BNESolverContext<Value, Bid> context;
	
	public Integrator(BNESolverContext<Value, Bid> context) {
		this.context = context;
	}

	// compute the utility of player i when bidding b while having value v, given that other players play strategies strats. 
	// Note that strategies[i] is ignored.
	public abstract double computeExpectedUtility(int i, Value v, Bid b, List<Strategy<Value, Bid>> strats);
}
