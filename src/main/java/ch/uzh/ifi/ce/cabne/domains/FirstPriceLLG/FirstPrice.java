package ch.uzh.ifi.ce.cabne.domains.FirstPriceLLG;


import ch.uzh.ifi.ce.cabne.domains.Mechanism;


public class FirstPrice implements Mechanism<Double, Double> {

	@Override
	public double computeUtility(int i, Double v, Double[] bids) {
		// In this mechanism, we assume that all players bid only on their bundle of interest.
		// bids is therefore an array of length 3.
				
		if (bids[2] > bids[0] + bids[1]) {
        	// global player wins
			if (i == 2) {
				return v - bids[2];
			} else {
				return 0.0;
			}
        } else if (bids[2] == bids[0] + bids[1]) {
        	// tie: 50-50 chance of winning
        	return 0.5*(v - bids[i]);
        } else {
        	if (i == 2) {
        		return 0.0;
        	} else {
		    	return v - bids[i];
        	}
        }
    }
}
