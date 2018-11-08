package ch.uzh.ifi.ce.cabne.randomsampling;

import java.util.Iterator;

public interface RandomGenerator {
	public Iterator<double[]> nextVectorIterator();
	
	default public void advance() {
		// this is for common random numbers, per default it does nothing
	};
}
