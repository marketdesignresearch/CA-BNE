package ch.uzh.ifi.ce.cabne.randomsampling;

import java.util.Iterator;


public class NaiveRandomGenerator implements RandomGenerator {
	int dimension;

	public NaiveRandomGenerator(int dimension) {
		this.dimension = dimension;
	}

	public double[] nextVector() {
		double res[] = new double[dimension];
		for (int i=0; i<dimension; i++) {
			res[i] = Math.random();
		}
		return res;
	}

	@Override
	public Iterator<double[]> nextVectorIterator() {		
		Iterator<double[]> it = new Iterator<double[]>() {
			
            @Override
            public boolean hasNext() {
                return true;
            }

			@Override
			public double[] next() {
				return nextVector();
			}
		};
		return it;
	}
}
