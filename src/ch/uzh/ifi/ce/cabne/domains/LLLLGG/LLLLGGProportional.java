package ch.uzh.ifi.ce.cabne.domains.LLLLGG;


public class LLLLGGProportional extends LLLLGGMechanism {
	static LLLLGGWD WD = new LLLLGGWD(); 

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
		// HACK: if utility is somehow smaller than 0, (i.e. - infinity), just return 0
		// TODO: investigate this, possibly caused by scalarProd being 0?
		if (Double.isNaN(utility)) {
			utility = 0.0;
		}
		if (Math.abs(utility) > 1000.) {
			utility = 0.0;
		}
		return utility;
	}
	
	public double[] projectToCore(Double[][] bids, int[] alloc) {
		// compute the vector pointing from the origin towards the winning bids.
		double[] bidDirection = new double[6];
		for (int bundle : alloc) {
			int winner = bundle/2;
			bidDirection[winner] = bids[winner][bundle%2];
		}
				
		int winners = encodeWinners(alloc);
		
		// Iterate over all alternative allocations, generate core constraints, find the core constraint with 
		// the intersection furthest up the bidDirection
		double maxMultiplier = 0.0;
		for (int[] alternativeAlloc : LLLLGGWD.subsolutions) {
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
				continue;
			}
			
			// Indicator vector of core constraint has a 1 entry for every bidder who is not part of the coalition,
			// i.e. it has an identical direction to the normal vector of the constraint hyperplane, but is scaled such
			// that moving a point x along the vector by 1 unit increases the manhattan norm of x by 1.
			// The scalar product with this vector tells us by how much the payment increases when we go 1 unit up 
			// along the bidDirection (note that the latter doesn't need to be normalized).
			double scalarProd = 0.0;
			for (int i=0; i<6; i++) {
				int coefficient = 1 - (coalition>>i)%2;
				scalarProd += coefficient * bidDirection[i];
			}
			
			// By how much we need to multiply bidDirection until we hit the core constraint?
			// This is guaranteed to be a number between 0 and 1.
			// Note that scalarProd cannot be 0, since we filter out coalitions which are supersets of the winners.
			double multiplier = valueDiff / scalarProd;
			maxMultiplier = Math.max(maxMultiplier, multiplier);
		}
		
		// scale bids down until they hit the highest core constraint.
		for (int i=0; i<6; i++) {
			bidDirection[i] *= maxMultiplier;
		}
		
		return bidDirection;
	}
}
