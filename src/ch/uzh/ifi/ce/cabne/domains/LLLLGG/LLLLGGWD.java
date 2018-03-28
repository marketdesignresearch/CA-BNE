package ch.uzh.ifi.ce.cabne.domains.LLLLGG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LLLLGGWD {
	static final double numericalPrecision = 1e-8;
	
	static final int[][] solutions = new int[][]{
		// 4 local bidders win
    	new int[]{0, 2, 4, 6}, 	// AB CD EF GH
    	new int[]{1, 3, 5, 7}, 	// BC DE FG HA
    	
    	// a global bidder wins (2 possibilities for each global bundle)
    	new int[]{4, 6, 8},     // EF GH ABCD
    	new int[]{0, 2, 9},    	// AB CD EFGH
    	new int[]{0, 6, 10},   	// AB GH CDEF
    	new int[]{2, 4, 11},    // CD EF GHAB
    	new int[]{5, 8},        // FG ABCD
    	new int[]{1, 9},		// BC EFGH 
    	new int[]{7, 10},       // HA CDEF
    	new int[]{3, 11},       // DE GHAB

    	// 3 locals win. This implies that 2 locals have adjacent bundles, and one doesn't. 
    	// there are 8 possibilities (choose bundle that has no adjacent bundles, the rest is determined)
    	new int[]{0, 2, 5},    	// AB CD FG
    	new int[]{2, 4, 7},     // CD EF HA
    	new int[]{1, 4, 6},		// BC EF GH
    	new int[]{0, 3, 6},    	// AB DE GH
    	new int[]{0, 3, 5},    	// AB DE FG
    	new int[]{2, 5, 7},     // CD FG HA
    	new int[]{1, 3, 6},		// BC DE GH
    	new int[]{1, 4, 7},		// BC EF HA
	};
	
    static final int[][] subsolutions = new int[][]{
        new int[]{0},
        new int[]{1},
        new int[]{2},
        new int[]{3},
        new int[]{4},
        new int[]{5},
        new int[]{6},
        new int[]{7},
        new int[]{8},
        new int[]{9},
        new int[]{10},
        new int[]{11},
        new int[]{0,2},
        new int[]{0,3},
        new int[]{0,4},
        new int[]{0,5},
        new int[]{0,6},
        new int[]{0,9},
        new int[]{0,10},
        new int[]{1,3},
        new int[]{1,4},
        new int[]{1,5},
        new int[]{1,6},
        new int[]{1,7},
        new int[]{1,9},
        new int[]{2,4},
        new int[]{2,5},
        new int[]{2,6},
        new int[]{2,7},
        new int[]{2,9},
        new int[]{2,11},
        new int[]{3,5},
        new int[]{3,6},
        new int[]{3,7},
        new int[]{3,11},
        new int[]{4,6},
        new int[]{4,7},
        new int[]{4,8},
        new int[]{4,11},
        new int[]{5,7},
        new int[]{5,8},
        new int[]{6,8},
        new int[]{6,10},
        new int[]{7,10},
        new int[]{0,2,4},
        new int[]{0,2,5},
        new int[]{0,2,6},
        new int[]{0,2,9},
        new int[]{0,3,5},
        new int[]{0,3,6},
        new int[]{0,4,6},
        new int[]{0,6,10},
        new int[]{1,3,5},
        new int[]{1,3,6},
        new int[]{1,3,7},
        new int[]{1,4,6},
        new int[]{1,4,7},
        new int[]{1,5,7},
        new int[]{2,4,6},
        new int[]{2,4,7},
        new int[]{2,4,11},
        new int[]{2,5,7},
        new int[]{3,5,7},
        new int[]{4,6,8},
        new int[]{0,2,4,6},
        new int[]{1,3,5,7},
     };

    public List<int[]> solveWD(Double[][] bids) {
        double maxWelfare = -1;
        double[] welfares = new double[solutions.length];
        for (int j=0; j < solutions.length; j++) {
            double welfare = Arrays.stream(solutions[j]).mapToDouble(i -> bids[i/2][i%2]).sum();
            welfares[j] = welfare;
            maxWelfare = Math.max(maxWelfare, welfare);
        }

        List<int[]> result = new ArrayList<>();
        for (int j=0; j < solutions.length; j++) {
            if (welfares[j] > maxWelfare - numericalPrecision) {
            	result.add(solutions[j]);
            }
        }
        return result;
    }

    public double computeWelfare(Double[][] bids) {
        double maxWelfare = -1;
        for (int j=0; j < solutions.length; j++) {
            double welfare = Arrays.stream(solutions[j]).mapToDouble(i -> bids[i/2][i%2]).sum();
            maxWelfare = Math.max(maxWelfare, welfare);
        }
        return maxWelfare;
    }
    
    public double[] computeVCG(Double[][] bids, int[] allocation) {
    	double totalValue = Arrays.stream(allocation).mapToDouble(i -> bids[i/2][i%2]).sum();
    	Double[][] bidsClone = bids.clone(); // make sure we own this object
    	Double[] zeroBid = new Double[]{0.0, 0.0};

    	// Note: this loop doesn't set vcg payments for bidders who win nothing, and in java arrays are initialized to 0
        double[] vcgPayments = new double[bids.length];
        for (int bundleIndex : allocation) {
        	int i = bundleIndex/2;
        	// set bids of this bidder to 0, then solve subproblem
        	bidsClone[i] = zeroBid;
        	double valueWithoutI = computeWelfare(bidsClone);
        	bidsClone[i] = bids[i];
        	
        	// vcg payment is: (total value of the allocation without i) - (total value of true allocation - value of i)
        	vcgPayments[i] = valueWithoutI - (totalValue - bids[i][bundleIndex%2]);   	
        }
        
        return vcgPayments;
    }
}
