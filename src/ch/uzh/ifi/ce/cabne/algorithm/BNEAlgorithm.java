package ch.uzh.ifi.ce.cabne.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.ce.cabne.BR.BRCalculator;
import ch.uzh.ifi.ce.cabne.BR.Verifier;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public class BNEAlgorithm<Value, Bid> {

	public enum IterationType {INNER, OUTER, VERIFICATION};
	
	private int nBidders;
	private BNESolverContext<Value, Bid> context;

	Map<Integer, Strategy<Value, Bid>> initialStrategies = new HashMap<>();
	
	int[] canonicalBidders;
	boolean[] updateBidder;
	

	private BNEAlgorithmCallback<Value, Bid> callback; 
	
	public BNEAlgorithm(int nBidders, BNESolverContext<Value, Bid> context) {
		this.nBidders = nBidders;
		canonicalBidders = new int[nBidders];
		updateBidder = new boolean[nBidders];
		for (int i=0; i<nBidders; i++) {
			canonicalBidders[i] = i;
			updateBidder[i] = true;
		}
		this.context = context;
	}

	public void setContext(BNESolverContext<Value, Bid> context) {
		this.context = context;
	}
	
	public void setCallback(BNEAlgorithmCallback<Value, Bid> callback) {
		this.callback = callback;
	}
	
	public void setInitialStrategy(int bidder, Strategy<Value, Bid> initialStrategy) {
		initialStrategies.put(bidder, initialStrategy);
	}
	
	public void makeBidderNonUpdating(int bidder) {
		updateBidder[bidder] = false;
	}
	
	public void makeBidderSymmetric(int bidder, int canonicalBidder) {
		if (canonicalBidders[canonicalBidder] != canonicalBidder) {
			throw new RuntimeException("Tried to add symmetric bidder but canonical bidder is not a primary bidder");
		}
		canonicalBidders[bidder] = canonicalBidder;
	}
	
	private void callbackAfterIteration(int iteration, IterationType type, List<Strategy<Value, Bid>> strategies, double epsilon) {
		if (callback != null) {
			callback.afterIteration(iteration, type, strategies, epsilon);
		}	
	}
	
	private double computeBestResponse(List<Strategy<Value, Bid>> strategies, BRCalculator<Value, Bid> brc) {
		double highestEpsilon = 0.0;
		
		// compute best responses for players where this is needed
		Map<Integer, Strategy<Value, Bid>> newStrategies = new HashMap<>();
		for (int i=0; i<nBidders; i++) {
			if (canonicalBidders[i] == i && updateBidder[i]) {
				// this is a canonical bidder whose strategy should be updated
				BRCalculator.Result<Value, Bid> result = brc.computeBR(i, strategies);
				Strategy<Value, Bid> s = result.br;	
				highestEpsilon = Math.max(highestEpsilon, result.epsilonAbs);
				newStrategies.put(i, s);
			}
		}

		// update strategies
		for (int i=0; i<nBidders; i++) {
			if (updateBidder[i]) {
				strategies.set(i, newStrategies.get(canonicalBidders[i]));
			}
		}
		
		return highestEpsilon;
	}	
	
	private double verify(List<Strategy<Value, Bid>> strategies, Verifier<Value, Bid> verifier) {
		double highestEpsilon = 0.0;
		int gridsize = context.getIntParameter("gridsize");
		
		// Convert strategies if needed (e.g. to PWC).
		// Note that if some player is non-updating (i.e. truthful), their strategy doesn't need to be converted.
		List<Strategy<Value, Bid>> sConverted = new ArrayList<>();
		for (int i=0; i<nBidders; i++) {
			if (updateBidder[i]) {
				sConverted.add(verifier.convertStrategy(gridsize, strategies.get(i)));
			} else {
				sConverted.add(strategies.get(i));
			}
		}
		
		// compute best responses for players where this is needed
		for (int i=0; i<nBidders; i++) {
			if (canonicalBidders[i] == i && updateBidder[i]) {
				// this is a canonical bidder whose epsilon should be computed
				double epsilon = verifier.computeEpsilon(gridsize, i, strategies.get(i), sConverted);
				highestEpsilon = Math.max(highestEpsilon, epsilon);
			}
		}
		
		return highestEpsilon;
	}

	public void run() {

		int maxIters = context.getIntParameter("maxiters");
		double targetEpsilon = context.getDoubleParameter("epsilon");
		double highestEpsilon = Double.POSITIVE_INFINITY;
		BRCalculator<Value, Bid> brc;
		
		// create list of strategies
		List<Strategy<Value, Bid>> strategies = new ArrayList<>();
		for (int i=0; i<nBidders; i++) {
			strategies.add(initialStrategies.get(canonicalBidders[i]));
		}
		
		context.activateConfig("innerloop"); // this allows the callback to assume some config is always active.
		callbackAfterIteration(0, IterationType.INNER, strategies, highestEpsilon);
		
		int iteration = 1;
		int lastOuterIteration = 1;
		
		while (iteration <= maxIters) {
			// This is the outer loop. First thing we do is go into the inner loop
			while (iteration <= maxIters) {
				// This is the inner loop.				
				context.activateConfig("innerloop");
				brc = context.brc;
				
				highestEpsilon = computeBestResponse(strategies, brc);
				context.advanceRngs();
				
				callbackAfterIteration(iteration, IterationType.INNER, strategies, highestEpsilon);

				iteration++;
				if (highestEpsilon <= 0.8*targetEpsilon && iteration >= lastOuterIteration + 3) {
					break;
				}
			}
			
			if (iteration > maxIters) {
				return;
			}
						
			lastOuterIteration = iteration;
			
			context.activateConfig("outerloop");
			brc = context.outerBRC;
			if (brc == null) {
				brc = context.brc;
			}
			
			highestEpsilon = computeBestResponse(strategies, brc);
			context.advanceRngs();
			
			callbackAfterIteration(iteration, IterationType.OUTER, strategies, highestEpsilon);

			iteration++;
			if (highestEpsilon <= targetEpsilon) {
				break;
			}
		}
		
		boolean converged = highestEpsilon <= targetEpsilon;
		if (converged) {
			context.activateConfig("verificationstep");

			highestEpsilon = verify(strategies, context.verifier);

			callbackAfterIteration(iteration, IterationType.VERIFICATION, strategies, highestEpsilon);
		}
    }
	
}
