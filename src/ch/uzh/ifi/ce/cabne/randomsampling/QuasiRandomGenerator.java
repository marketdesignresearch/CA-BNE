package ch.uzh.ifi.ce.cabne.randomsampling;

import java.util.Iterator;


import org.apache.commons.math3.random.SobolSequenceGenerator;

public class QuasiRandomGenerator implements RandomGenerator {
	SobolSequenceGenerator generator;

	public QuasiRandomGenerator(int dimension) {
		generator = new SobolSequenceGenerator(dimension);
	}

	public QuasiRandomGenerator(int dimension, int skip) {
		this(dimension);
		generator.skipTo(skip);
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
