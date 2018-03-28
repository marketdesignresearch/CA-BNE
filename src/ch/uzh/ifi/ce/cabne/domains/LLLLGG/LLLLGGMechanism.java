package ch.uzh.ifi.ce.cabne.domains.LLLLGG;


import ch.uzh.ifi.ce.cabne.domains.Mechanism;


public abstract class LLLLGGMechanism implements Mechanism<Double[], Double[]> {
	static LLLLGGWD wd = new LLLLGGWD();

	@Override
	public double computeUtility(int i, Double[] v, Double[][] bids) {
		double utility = 0.0;
		double count = 0.0;
		for (int[] alloc : wd.solveWD(bids)) {
			for (int bundle : alloc) {
				if (bundle/2 == i) utility += v[bundle%2] - bids[i][bundle%2];
			}
			count++;
		}
		
		return utility / count;
	}
	
	// This class handles winner determination and ties. Subclasses just need to implement the utility given a specific
	// allocation.
	protected abstract double computeUtility(int i, Double[] v, Double[][] bids, int[] alloc);
	
	
	
	public int encodeWinners(int[] assignment) {
		// encode the winners of assignment (given as an array of bundle indices) into a set of flags
		int result = 0;
		for (int bundleindex : assignment) {
			int bidder = bundleindex/2;
			result |= 1 << bidder;
		}
		return result;
	}
}
