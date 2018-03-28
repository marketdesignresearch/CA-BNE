package ch.uzh.ifi.ce.cabne.domains.LLLLGG.CCG;

import ch.uzh.ifi.ce.cca.NumericalAllocation;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by buenz on 11.01.16.
 */
public class MidSizeDomainWD {
    private int[] sol1 = new int[]{0, 2, 4, 6};
    private int[] sol2 = new int[]{0, 2, 5};
    private int[] sol3 = new int[]{0, 2, 9};
    private int[] sol4 = new int[]{0, 3, 6};
    private int[] sol5 = new int[]{0, 3, 5};
    private int[] sol6 = new int[]{0, 6, 10};
    private int[] sol7 = new int[]{1, 3, 5, 7};
    private int[] sol8 = new int[]{7, 10};
    private int[] sol9 = new int[]{2, 4, 7};
    private int[] sol10 = new int[]{2, 5, 7};
    private int[] sol11 = new int[]{4, 6, 8};
    private int[] sol12 = new int[]{5, 8};
    private int[] sol13 = new int[]{2, 4, 11};
    private int[] sol14 = new int[]{3, 11};
    private int[] sol15 = new int[]{1, 3, 6};
    private int[] sol16 = new int[]{1, 4, 6};
    private int[] sol17 = new int[]{1, 9};
    private int[] sol18 = new int[]{1, 4, 7};

    private int[][] solutions = new int[][]{sol1, sol2, sol3, sol4, sol5, sol6, sol7, sol8, sol9, sol10, sol11, sol12, sol13, sol14, sol15, sol16, sol17, sol18};

    public NumericalAllocation solveWD(BigDecimal[] bids) {

        int[] maxSol = null;
        BigDecimal objectiveValue = new BigDecimal(-1);
        for (int[] sol : solutions) {
            BigDecimal value = Arrays.stream(sol).mapToObj(i -> bids[i]).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (value.compareTo(objectiveValue) > 0) {
                objectiveValue = value;
                maxSol = sol;
            }
        }
        return new NumericalAllocation(bids, maxSol, objectiveValue);

    }


    public BigDecimal[] computeVCG(NumericalAllocation allocation) {

        BigDecimal[] bids = allocation.getBids();
        BigDecimal[] vcgPayments = new BigDecimal[allocation.getAllocation().length];
        int index = 0;
        for (int winningBid : allocation.getAllocation()) {
            BigDecimal bidderAllocation = bids[winningBid];
            bids[winningBid] = BigDecimal.ZERO;
            int otherBid = (winningBid / 2) * 2 + (1 - winningBid % 2);
            BigDecimal otherBidValue = bids[otherBid];
            bids[otherBid] = BigDecimal.ZERO;

            BigDecimal allocationValueWithout = solveWD(bids).getAllocaionValue();
            BigDecimal vcgPayment = allocationValueWithout.subtract(allocation.getAllocaionValue()).add(bidderAllocation);
            vcgPayments[index++] = vcgPayment;
            bids[winningBid] = bidderAllocation;
            bids[otherBid] = otherBidValue;
        }
        return vcgPayments;
    }


}
