package ch.uzh.ifi.ce.cabne.BR;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import ch.uzh.ifi.ce.cabne.Helpers;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Optimizer;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.strategy.UnivariatePWLStrategy;

// This class takes an arbitrary 1d strategy profile and computes PWL BR's, where control points are placed adaptively to
// find kinks in the function.


public class AdaptivePWLBRCalculator implements BRCalculator<Double, Double> {

    private InterpolationPointGenerator<Double, Double> pointGenerator = new UnivariateIterativeInterpolation();
	BNESolverContext<Double, Double> context;
	
	
	public AdaptivePWLBRCalculator(BNESolverContext<Double, Double> context) {
		this.context = context;
	}


	public Result<Double, Double> computeBR(int i, List<Strategy<Double, Double>> s) {
		int nPoints = Integer.parseInt(context.config.get("adaptivegridsize"));
		
		NavigableMap<Double, Double> pointwiseBRs = new TreeMap<>();
        NavigableMap<Double, Double> undampenedPointwiseBRs = new TreeMap<>();
		double epsilonAbs = 0.0;
		double epsilonRel = 0.0;
		
		double vmin = 0.0;
        double vmax = s.get(i).getMaxValue();
        while (!pointGenerator.isConverged(undampenedPointwiseBRs, vmin, vmax, nPoints)) {

            double[] nextInterpolationPoints = pointGenerator.getNextInterpolationPoints(undampenedPointwiseBRs, vmin, vmax, nPoints);
            for (double v : nextInterpolationPoints) {
            	
            	double oldbid = s.get(i).getBid(v);
            	Optimizer.Result<Double> result = context.optimizer.findBR(i, v, oldbid, s);

    			Double newbid = context.updateRule.update(v, oldbid, result.bid, result.oldutility, result.utility);
                pointwiseBRs.put(v, newbid);
                undampenedPointwiseBRs.put(v, result.bid);

    			epsilonAbs = Math.max(epsilonAbs, Helpers.absoluteUtilityLoss(result.oldutility, result.utility));
    			epsilonRel = Math.max(epsilonRel, Helpers.relativeUtilityLoss(result.oldutility, result.utility));
            }


        }
		
		return new Result<Double, Double>(new UnivariatePWLStrategy(pointwiseBRs), epsilonAbs, epsilonRel);
	}

}
