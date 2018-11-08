package ch.uzh.ifi.ce.cabne.domains.LLG;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public class LLGSampler extends BidSampler<Double, Double> {

	
	public LLGSampler(BNESolverContext<Double, Double> context) {
		super(context);
	}

	public Iterator<Sample> conditionalBidIterator(int i, Double v, Double b, List<Strategy<Double, Double>> s) {
		// get an LLG sample, assuming that local bidders are independently distributed u.a.r. in [0,1]
		// and global bidder is u.a.r. in [0,2]
		
		// NOTE: this assumes that global bidder plays truthful, which is only the case with a core-selecting rule.
		// It's not enough to add a separate code path for i == 2 to make it work for a strategic global bidder.
		if (i == 2) {
			throw new RuntimeException("This sampler assumes that the global player is truthful.");
		}
		
		final int localopponent = (i + 1) % 2;
		Iterator<double[]> rngiter = context.getRng(2).nextVectorIterator();
		Strategy<Double, Double> slocal = s.get(localopponent);
		Strategy<Double, Double> sglobal = s.get(2);
		double localMax = slocal.getMaxValue();
		double globalMax = sglobal.getMaxValue();
		
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
				result[i] = b;
				result[localopponent] = slocal.getBid(r[0] * localMax); 
				
				// bid of global player, ignoring the area where the global player wins, and adjusting the density
				// accordingly (basically importance sampling)
				double globalbound = Math.min(b + result[localopponent], globalMax);
				double density = globalbound / globalMax / globalMax; // would be 1 / globalMax without importance sampling
				result[2] = globalbound * r[1];
				
				return new Sample(density, result);
			}
		};
		return it;
	}
}
