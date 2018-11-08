package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.List;

import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;


public class UnivariateBrentSearch extends Optimizer<Double, Double> {
	UnivariateOptimizer optimizer = new BrentOptimizer(1e-9, 1e-9);
	
	public UnivariateBrentSearch(BNESolverContext<Double, Double> context) {
		super(context);
	}


	@Override
	public Result<Double> findBR(int i, Double v, Double currentBid, List<Strategy<Double, Double>> strats) {		
		if (v == 0.0) {
			// this is needed because Brent search doesn't like a search range from 0 to 0
			return new Result<>(0.0, 0.0, 0.0);
		}
		
		UnivariateObjectiveFunction objectiveFunction = new UnivariateObjectiveFunction(
				bid -> {
					return context.integrator.computeExpectedUtility(i, v, bid, strats);
				}
		);
		
		UnivariatePointValuePair optimum = optimizer.optimize(new MaxEval(50), objectiveFunction, GoalType.MAXIMIZE,
													          new SearchInterval(0.0, 2.0*currentBid, currentBid));
		
		
		double oldutility = context.integrator.computeExpectedUtility(i, v, currentBid, strats);
		
		return new Result<>(optimum.getPoint(), optimum.getValue(), oldutility);
	}
	
}
