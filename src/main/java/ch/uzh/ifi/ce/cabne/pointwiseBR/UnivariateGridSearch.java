package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

// this is a very naive optimization class, superceded by brent search, pattern search, etc

public class UnivariateGridSearch extends Optimizer<Double, Double> {
	
	public UnivariateGridSearch(BNESolverContext<Double, Double> context) {
		super(context);
	}


	@Override
	public Result<Double> findBR(int i, Double v, Double currentBid, List<Strategy<Double, Double>> strats) {
		Double bestbid = 0.0;
		double bestutility = 0.0;
		for (double bid = currentBid * 0.5; bid < currentBid * 1.55; bid+=currentBid*0.05) {
			double utility = context.integrator.computeExpectedUtility(i, v, bid, strats);
			if (utility > bestutility) {
				bestutility = utility;
				bestbid = bid;
			}
		}
		
		double oldutility = context.integrator.computeExpectedUtility(i, v, currentBid, strats);
		
		return new Result<>(bestbid, bestutility, oldutility);
	}
}
