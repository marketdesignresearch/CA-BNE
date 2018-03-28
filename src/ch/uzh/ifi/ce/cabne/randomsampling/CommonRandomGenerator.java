package ch.uzh.ifi.ce.cabne.randomsampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.random.SobolSequenceGenerator;


public class CommonRandomGenerator implements RandomGenerator {
	int dimension, batchsize;
	SobolSequenceGenerator generator;
	List<double[]> cachedValues;
	

	public CommonRandomGenerator(int dimension) {
		this.dimension = dimension;
		batchsize = 10000;
		cachedValues = new ArrayList<>();
		generator = new SobolSequenceGenerator(dimension);
	}
	
	public CommonRandomGenerator(int dimension, int skip) {
		this(dimension);
		generator.skipTo(skip);
	}

	@Override
	public Iterator<double[]> nextVectorIterator() {
		
		Iterator<double[]> it = new Iterator<double[]>() {
			private int index = -1;
			
            @Override
            public boolean hasNext() {
                return true;
            }

			@Override
			public double[] next() {
				index++;
				if (index >= cachedValues.size()) {
					moreSamples();
				}
				return cachedValues.get(index);
			}
		};
		return it;
	}
	
	public void advance() {
		// note that this is not thread-safe in any way. Also if iterators created before calling this are used after
		// calling this, weird things happen.
		// TODO: could add safety against this, e.g. by letting each batch of iterators have their own cache
		// cachedValues = new... etc, which deallocates old cache as soon as no references exist anymore
		cachedValues.clear();
	}
	
	private void moreSamples() {
		for (int i=0; i<batchsize; i++) {
			cachedValues.add(generator.nextVector());
		}
	}
}
