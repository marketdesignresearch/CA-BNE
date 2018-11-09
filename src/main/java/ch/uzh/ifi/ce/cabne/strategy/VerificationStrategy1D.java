package ch.uzh.ifi.ce.cabne.strategy;

/*
 * Strategy used for verification. It takes an arbitrary strategy, and converts it to a piecewise
 * constant one on a grid. The grid is spaced closer together for high valuations, as controlled 
 * by a factor in (0,1): the highest and second-highest grid points are spaced factor*avg apart from 
 * each other, where avg is the distance they would be apart on a regular grid.
 * 
 * This is the implementation in one dimension.
 */

public class VerificationStrategy1D implements Strategy<Double, Double> {
	final Strategy<Double, Double> underlyingStrategy;
	final int N;
	
	QuadraticGridHelper helper;
	
	public VerificationStrategy1D(int N, double factor, Strategy<Double, Double> underlyingStrategy) {
		super();
		this.underlyingStrategy = underlyingStrategy;
		this.N = N;
		
		// compute polynomial coefficients
		helper = new QuadraticGridHelper(N, getMaxValue(), factor);
	}

	@Override
	public Double getBid(Double v) {
		Double tmp = helper.evalPolynomial(v);
		Double lookupV = helper.invertPolynomial(tmp.intValue());
		return underlyingStrategy.getBid(lookupV);
	}

	@Override
	public Double getMaxValue() {
		return underlyingStrategy.getMaxValue();
	}
	
	public Double getGridpoint(Integer gridpoint) {
		if (gridpoint == 0) return 0.0;
		if (gridpoint == N-1) return underlyingStrategy.getMaxValue();
		return helper.invertPolynomial(gridpoint);
	}
	
}
