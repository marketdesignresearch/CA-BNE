package ch.uzh.ifi.ce.cabne.algorithm;

import java.util.List;

import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public interface BNEAlgorithmCallback<Value, Bid> {

	public void afterIteration(int iteration, BNEAlgorithm.IterationType type, List<Strategy<Value, Bid>> strategies, double epsilon);
	
}
