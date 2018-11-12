package ch.uzh.ifi.ce.cabne.BR;

import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.helpers.UtilityHelpers;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Optimizer;
import ch.uzh.ifi.ce.cabne.strategy.GridStrategy2D;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

/*
 * Best response calculator which assumes that values and bids are 2-dimensional arrays of Doubles.
 * Constructs a GridStrategy2D as the best response.
 */
public class Grid2DBRCalculator implements BRCalculator<Double[], Double[]> {
	BNESolverContext<Double[], Double[]> context;

	public Grid2DBRCalculator(BNESolverContext<Double[], Double[]> context) {
		this.context = context;
	}

	public Result<Double[], Double[]> computeBR(int i, List<Strategy<Double[], Double[]>> s) {
		int nPoints = Integer.parseInt(context.config.get("gridsize"));

		RealMatrix left = new Array2DRowRealMatrix(nPoints+1, nPoints+1);
		RealMatrix right = new Array2DRowRealMatrix(nPoints+1, nPoints+1);
		double epsilonAbs = 0.0;
		double epsilonRel = 0.0;
		
		Double[] maxValue = s.get(i).getMaxValue();

		for (int x = 0; x<=nPoints; x++) {
			for (int y = 0; y<=nPoints; y++) {
				Double[] v = new Double[]{((double) x)/nPoints * maxValue[0], ((double) y)/nPoints * maxValue[1]};
				Double[] oldbid = s.get(i).getBid(v);
				
				Optimizer.Result<Double[]> result = context.optimizer.findBR(i, v, oldbid, s);
				epsilonAbs = Math.max(epsilonAbs, UtilityHelpers.absoluteLoss(result.oldutility, result.utility));
				epsilonRel = Math.max(epsilonRel, UtilityHelpers.relativeLoss(result.oldutility, result.utility));
				Double[] newbid = context.updateRule.update(v, oldbid, result.bid, result.oldutility, result.utility);
				left.setEntry(x, y, newbid[0]);
				right.setEntry(x, y, newbid[1]);
			}
		}
		return new Result<Double[], Double[]>(new GridStrategy2D(left, right, maxValue[0], maxValue[1]), epsilonAbs, epsilonRel);
	}
}
