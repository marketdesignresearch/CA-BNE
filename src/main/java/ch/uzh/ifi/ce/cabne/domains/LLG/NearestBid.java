package ch.uzh.ifi.ce.cabne.domains.LLG;


import ch.uzh.ifi.ce.cabne.domains.Mechanism;


public class NearestBid implements Mechanism<Double, Double> {

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
            if (bids[2] < bids[i] - bids[(i+1)%2]) {
            	// projection onto MRC would make the other's payment negative, so i pays everything
            	payment = bids[2];
            } else if (bids[2] < bids[(i+1)%2] - bids[i]) {
            	// projection onto MRC would make i's payment negative, so the other pays everything
            	payment = 0;
            } else {
            	// locals split the money "left on the table"
            	payment = (bids[2] - bids[0] - bids[1])/2 + bids[i];
            }
        	return v - payment;
        }
    }
}
