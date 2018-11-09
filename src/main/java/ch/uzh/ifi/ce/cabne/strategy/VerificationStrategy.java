package ch.uzh.ifi.ce.cabne.strategy;

/*
 * Strategy used for verification. It takes an arbitrary strategy, and converts it to a piecewise
 * constant one on a grid. The grid is spaced closer together for high valuations, as controlled 
 * by a factor in (0,1): the highest and second-highest grid points are spaced factor*avg apart from 
 * each other, where avg is the distance they would be apart on a regular grid.
 *  
 * This is the implementation in higher dimensions (with "separable" grid spacings).
 */

public class VerificationStrategy implements Strategy<Double[], Double[]> {
	final Strategy<Double[], Double[]> underlyingStrategy;
	final int N;
	final int d;
	
	QuadraticGridHelper[] helpers;
	
	public VerificationStrategy(int N, double factor, Strategy<Double[], Double[]> underlyingStrategy) {
		super();
		this.underlyingStrategy = underlyingStrategy;
		this.N = N;
		
		// read dimension
		d = underlyingStrategy.getMaxValue().length;
		
		// compute polynomial coefficients
		helpers = new QuadraticGridHelper[d];
		for (int j=0; j<d; j++) {
			helpers[j] = new QuadraticGridHelper(N, getMaxValue()[j], factor);
		}
	}

	@Override
	public Double[] getBid(Double[] v) {
		Double[] lookupV = new Double[d];
		for (int j=0; j<d; j++) {
			Double tmp = helpers[j].evalPolynomial(v[j]);
			lookupV[j] = helpers[j].invertPolynomial(tmp.intValue());
		}
		return underlyingStrategy.getBid(lookupV);
	}

	@Override
	public Double[] getMaxValue() {
		return underlyingStrategy.getMaxValue();
	}
	
	public Double[] getGridpoint(Integer[] gridpoint) {
		Double[] result = new Double[d];
		for (int j=0; j<d; j++) {
			int y = gridpoint[j];
			if (y == 0) result[j] = 0.0;
			if (y == N-1) result[j] = underlyingStrategy.getMaxValue()[j];
			else result[j] = helpers[j].invertPolynomial(y);
		}
		return result;
	}
	
}
