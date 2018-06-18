package ch.uzh.ifi.ce.cabne.domains.LLG;


import ch.uzh.ifi.ce.cabne.domains.Mechanism;


public class Proportional implements Mechanism<Double, Double> {

	@Override
	public double computeUtility(int i, Double v, Double[] bids) {
		// In this mechanism, we assume that all players bid only on their bundle of interest.
		// bids is therefore an array of length 3.
				
		if (i==2) {
			// utility of global player
			if (bids[2] > bids[0] + bids[1]) {
				return v - bids[1] - bids[0];
			}
			return 0.0;
		} else if (bids[2] > bids[0] + bids[1]) {
        	// global player wins
            return 0.0;
        } else {
        	double payment;
        	if (bids[i] == 0.0) { 
        		payment = 0.0;
        	} else {
	    		// bidder pays a share of core constraint (= bid[2]) equal to the proportion of his bid
	            payment = bids[2] * bids[i] / (bids[0] + bids[1]);
        	}
        	return v - payment;
        }
    }
}
