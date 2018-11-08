package ch.uzh.ifi.ce.cabne.domains.LLG;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public class AusubelBaranovLLGSampler extends BidSampler<Double, Double> {
	final double alpha, gamma;
	
	public AusubelBaranovLLGSampler(double alpha, double gamma, BNESolverContext<Double, Double> context) {
		super(context);
		this.alpha = alpha;
		this.gamma = gamma;
	}

	public Iterator<Sample> conditionalBidIterator(int i, Double v, Double b, List<Strategy<Double, Double>> s) {		
		// NOTE: this assumes that global bidder plays truthful, which is only the case with a core-selecting rule.
		// It's not enough to add a separate code path for i == 2 to make it work for a strategic global bidder.
		if (i == 2) {
			throw new RuntimeException("This sampler assumes that the global player is truthful.");
		}
		
		Iterator<double[]> rngiter = context.getRng(2).nextVectorIterator();
		Iterator<double[]> rngitercorr = context.getRng(1).nextVectorIterator();
		

		Strategy<Double, Double> slocal = s.get((i + 1) % 2);
		
		Iterator<Sample> it = new Iterator<Sample>() {
			double ncorr = 0.0;
			double nuncorr = 0.0;
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Sample next() {
				double[] r;
				Double result[] = new Double[3];
				double bopponent, density;
				
				if (ncorr < nuncorr * (gamma / (1-gamma))) {
            		// next sample is correlated
            		ncorr++;
            		// 2 * r[0] is the global player's value
            		r = rngitercorr.next();
            		bopponent = slocal.getBid(v);
            		density = 1.0;            		
            	} else {
            		// next sample is uncorrelated
            		nuncorr++;
            		// r[1] is the other local player's value, 2 * r[0] is the global player's value
            		r = rngiter.next();
            		bopponent = slocal.getBid(r[1]); 
            		density = alphaDensity(r[1]);
            	}
            	
    			// bids of local players
				result[i] = b;
				result[(i + 1) % 2] = bopponent; 
				
				// bid of global player, ignoring the area where the global player wins, and adjusting the density
				// accordingly (importance sampling)
				double globalbound = Math.min(b + bopponent, 2.0);
				density *= 0.5 * globalbound/2.0;
				
				result[2] = globalbound * r[0];
				return new Sample(density, result);
			}
		};
		return it;
	}
	
	public double alphaDensity(double bid) {
		if (alpha == 1.0) {
			return 1.0;
		} else if (alpha == 2.0) {
			return 2.0 * bid;
		}
		return alpha * Math.pow(bid, alpha - 1.0);
	}
}
