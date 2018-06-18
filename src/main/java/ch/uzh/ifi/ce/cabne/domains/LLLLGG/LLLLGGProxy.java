package ch.uzh.ifi.ce.cabne.domains.LLLLGG;

import java.util.ArrayList;
import java.util.List;


public class LLLLGGProxy extends LLLLGGMechanism { 
	
	@Override
	public double computeUtility(int i, Double[] v, Double[][] bids, int[] alloc) {
		if ((encodeWinners(alloc)>>i)%2 == 0) {
			// if player i doesn't win anything, there is no need to compute the core payments
			return 0.0;
		}
		double[] corePayments = projectToCore(bids, alloc);
		double utility = -corePayments[i];
		for (int bundle : alloc) {
			if (bundle/2 == i) utility += v[bundle%2];
		}
		return utility;
	}
	
	public double[] projectToCore(Double[][] bids, int[] alloc) {
		int winners = encodeWinners(alloc); // bit array used to quickly discard irrelevant coalitions
		
		int[] activeBidders = new int[6];
		List<Double> sortedBids = new ArrayList<>();
		sortedBids.add(0.0);
		for (int bundle : alloc) {
			int i = bundle/2;
			double bid = bids[i][bundle%2]; 
			activeBidders[i] = bid > 0.0 ? 1 : 0;
			sortedBids.add(bid);
		}
		sortedBids.sort(null);

		double[] currentPoint = new double[6];
		int m = 0; // inner loop variable, which should *not* be reset when we enter the loop.
		for (int j=0; j<alloc.length; j++) {
			double segmentMaxPayment =  sortedBids.get(j+1) - sortedBids.get(j); // maximum extra payment allowed *per bidder* for this segment
			if (segmentMaxPayment == 0.0) {
				continue;
			}
			double highestMultiplier = 0.0;  // highest multiplier that occurred during intersections of this segment
			
			for (; m<LLLLGGWD.subsolutions.length; m++) {
				int[] alternativeAlloc = LLLLGGWD.subsolutions[m]; 
				int coalition = encodeWinners(alternativeAlloc);
				if ((coalition & winners) == coalition) {
					// coalition is a superset of the winners
					// we always have valueDiff < 0, otherwise alternativeAlloc would be more efficient than alloc.
					continue;
				}
				
				// Compute additional utility this coalition could get under the alternative allocation.
				// by subtracting existing utility from potential utility.
				double valueDiff = 0.0;
				for (int bundleIndex : alternativeAlloc) {
					valueDiff += bids[bundleIndex/2][bundleIndex%2];
				}
				for (Integer bundleIndex : alloc) {
					int i = bundleIndex/2;
					if ((coalition>>i)%2 == 1) {
						valueDiff -= bids[i][bundleIndex%2];
					}
				}
				if (valueDiff <= 0.0) {
					// coalition gets worse utility under alternativeAlloc than alloc
					continue;
				}
					
				// There are 2 values computed in this loop:
				//   - scalarProd tells us how much closer we get to the constraint per unit travelled along the segment. 
				//     It equals the number of active bidders who are not in the coalition.
				//   - currentPayment tells us how much payment the coalition is receiving from the lower point of the segment we're considering
				int scalarProd = 0;
				double currentPayment = 0.0;
				for (int i=0; i<6; i++) {
					int coefficient = 1 - (coalition>>i)%2;
					scalarProd += coefficient * activeBidders[i];
					currentPayment += coefficient * currentPoint[i];
				}

				// It's important to do this check because scalarProd == 0 is possible and leads to division by 0 later, 
				// but this can only happen in cases where the core constraint is satisfied by the current payment (and so we are allowed to skip it).
				if (currentPayment >= valueDiff - 1e-6) {
					continue;
				} else if (scalarProd == 0) {
					throw new RuntimeException("Unsatisfied core constraint, but it's not possible to increase the payment of any bidder without violating IR.");
				}
				
				// How far do we need to move along the segment until we reach valueDiff?
				double multiplier = (valueDiff - currentPayment) / scalarProd;
				if (multiplier > segmentMaxPayment) {
					// Intersection is beyond the end of the segment. 
					// Immediately go to next segment of payment curve and process the same core constraint again.
					highestMultiplier = segmentMaxPayment;
					break;
				}
				highestMultiplier = Math.max(highestMultiplier, multiplier);
			}
			
			// NOTE: if we have several bids of the same magnitude, we end up deactivating all of them at the same time, 
			// then skipping one or more iterations (because segmentMaxPayment == 0.0)
			for (int bundle : alloc) {
				int i = bundle/2;
				currentPoint[i] += activeBidders[i] * highestMultiplier;
				if (sortedBids.get(j+1).equals(bids[i][bundle%2])) {
					activeBidders[i] = 0; 
				}
			}
		}
		return currentPoint;
	}
}
