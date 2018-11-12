package ch.uzh.ifi.ce.cabne.verification;

import java.util.List;

import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public interface Verifier<Value, Bid> {
	
	public Strategy<Value, Bid> convertStrategy(int gridsize, Strategy<Value, Bid> s);
	
	// NOTE that the strategy profile s has had some strategies converted (e.g. to be piecewise constant).
	// We also pass in the original, unconverted strategy of player i, because this allows us to avoid numerical errors
	// in the strategy lookup of the verification points themselves.
	double computeEpsilon(int gridsize, int i, Strategy<Value, Bid> si, List<Strategy<Value, Bid>> s);
	
}
