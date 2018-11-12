package ch.uzh.ifi.ce.cabne.verification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.helpers.UtilityHelpers;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Optimizer;
import ch.uzh.ifi.ce.cabne.strategy.ConstantGridStrategy2D;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public class BoundingVerifier2D implements Verifier<Double[], Double[]> {
	BNESolverContext<Double[], Double[]> context;
	
	public BoundingVerifier2D(BNESolverContext<Double[], Double[]> context) {
		super();
		this.context = context;
	}

	public Strategy<Double[], Double[]> convertStrategy(int gridsize, Strategy<Double[], Double[]> s) {
		Double[] maxValue = s.getMaxValue();

		double[][] left = new double[gridsize][];
		double[][] right = new double[gridsize][];
		for (int j=0; j<gridsize; j++) {
			left[j] = new double[gridsize];
			right[j] = new double[gridsize];
			for (int k=0; k<gridsize; k++) {
				Double[] value = new Double[]{
						maxValue[0] * ((double) j) / (gridsize),
						maxValue[1] * ((double) k) / (gridsize)
						};
				Double[] bid = s.getBid(value);
				left[j][k] = bid[0];
				right[j][k] = bid[1];
			}
		}
		RealMatrix mLeft = MatrixUtils.createRealMatrix(left);
		RealMatrix mRight = MatrixUtils.createRealMatrix(right);
		return new ConstantGridStrategy2D(mLeft, mRight, maxValue[0], maxValue[1]);
	}
	
	@Override
	public double computeEpsilon(int gridsize, int i, Strategy<Double[], Double[]> si, List<Strategy<Double[], Double[]>> s) {	
		Double[] maxValue = si.getMaxValue();
		
		double cellEpsilon2D = 0.0;
		double boundaryEpsilon1D = 0.0;
		double boundaryEpsilon0D = 0.0;
		
		Map<Integer, Map<Integer, Optimizer.Result<Double[]>>> results = new HashMap<>();
		
		for (int j=0; j<gridsize; j++) {
			results.put(j, new HashMap<>());
			for (int k=0; k<gridsize; k++) {
				Double[] value = new Double[]{
						maxValue[0] * ((double) j) / (gridsize),
						maxValue[1] * ((double) k) / (gridsize)
						};
				
				// NOTE: it's important that the strategy lookup of the equilibrium bid is done using the original piecewise
				// linear strategy given by si, and not the modified piecewise constant strategy s.get(i)
				// If we look up the exact bottom-left point of a segment of a PWC strategy, we might miss it due to limited
				// numerical precision and land on an adjacent segment, which is significantly lower.
				// This amplifies the numerical error disproportionately.
				// In contrast, an error in the lookup of a PWL strategy tends to be better behaved. More importantly, since we
				// are doing the exact same strategy lookup here and in the convertStrategy method of this class, the bids we
				// get should be identical.
				Double[] equilibriumBid = si.getBid(value);
				Optimizer.Result<Double[]> result = context.optimizer.findBR(i, value, equilibriumBid, s);
				results.get(j).put(k, result);

				// compute epsilon bound on each 2d cell, then on the 1d boundaries, 
				// then on the 0d boundary (i.e. topmost grid point)
				double pointEpsilon = UtilityHelpers.absoluteLoss(result.oldutility, result.utility);
				if (j>0 && k>0) {
					double epsilon = result.utility - results.get(j-1).get(k-1).utility + pointEpsilon;
					cellEpsilon2D = Math.max(cellEpsilon2D, epsilon);
				}
				if (j==gridsize-1 && k>0) {
					double epsilon = result.utility - results.get(j).get(k-1).utility + pointEpsilon;
					boundaryEpsilon1D = Math.max(boundaryEpsilon1D, epsilon);
				}
				if (j>0 && k==gridsize-1) {
					double epsilon = result.utility - results.get(j-1).get(k).utility + pointEpsilon;
					boundaryEpsilon1D = Math.max(boundaryEpsilon1D, epsilon);
				}
				if (j==gridsize-1 && k==gridsize-1) {
					boundaryEpsilon0D = pointEpsilon;
				}
			}
		}
		return Math.max(Math.max(cellEpsilon2D, boundaryEpsilon1D), boundaryEpsilon0D);
	}
}