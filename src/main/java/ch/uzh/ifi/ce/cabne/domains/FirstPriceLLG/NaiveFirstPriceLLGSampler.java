package ch.uzh.ifi.ce.cabne.domains.FirstPriceLLG;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.strategy.UnivariatePWLStrategy;

/* 
 * Sampler that doesn't make use of importance sampling. Keep it here so that it's easy to assert the 
 * correctness of the more complex sampler.
 */

public class NaiveFirstPriceLLGSampler extends BidSampler<Double, Double> {

	
	public NaiveFirstPriceLLGSampler(BNESolverContext<Double, Double> context) {
		super(context);
	}

	public Iterator<Sample> conditionalBidIterator(int i, Double v, Double b, List<Strategy<Double, Double>> s) {
		if (i == 2) {
			Iterator<double[]> rngiter = context.getRng(2).nextVectorIterator();
			Strategy<Double, Double> slocal0 = s.get(0);
			UnivariatePWLStrategy slocal1 = (UnivariatePWLStrategy) s.get(1);
			double density = 1.0 / slocal0.getMaxValue() / slocal1.getMaxValue();
			
			Iterator<Sample> it = new Iterator<Sample>() {
				@Override
				public boolean hasNext() {
					return true;
				}

				@Override
				public Sample next() {
					double[] r = rngiter.next();
					Double result[] = new Double[3];
					
					// bids of local players
					result[0] = slocal0.getBid(r[0] * slocal0.getMaxValue());
					result[1] = slocal1.getBid(r[1] * slocal1.getMaxValue()); 
					result[2] = b;
										
					return new Sample(density, result);
				}
			};
			return it;
		}
		
		final int localopponent = (i + 1) % 2;
		Iterator<double[]> rngiter = context.getRng(2).nextVectorIterator();
		Strategy<Double, Double> slocal = s.get(localopponent);
		UnivariatePWLStrategy sglobal = (UnivariatePWLStrategy) s.get(2);
		double density = 1.0 / slocal.getMaxValue() / sglobal.getMaxValue();
		
		Iterator<Sample> it = new Iterator<Sample>() {
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Sample next() {
				double[] r = rngiter.next();
				Double result[] = new Double[3];
				
				result[i] = b;
				result[localopponent] = slocal.getBid(r[0] * slocal.getMaxValue()); 
				result[2] = sglobal.getBid(r[1] * sglobal.getMaxValue());
				
				return new Sample(density, result);
			}
		};
		return it;
	}
}
