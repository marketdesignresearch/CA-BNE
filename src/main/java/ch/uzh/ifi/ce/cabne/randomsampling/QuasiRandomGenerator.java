package ch.uzh.ifi.ce.cabne.randomsampling;

import java.util.Iterator;


import org.apache.commons.math3.random.RandomVectorGenerator;
import org.apache.commons.math3.random.SobolSequenceGenerator;

public class QuasiRandomGenerator implements RandomGenerator {
	RandomVectorGenerator generator;

	public QuasiRandomGenerator(int dimension) {
		generator = new SobolSequenceGenerator(dimension);
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
				return generator.nextVector();
			}
		};
		return it;
	}
}
