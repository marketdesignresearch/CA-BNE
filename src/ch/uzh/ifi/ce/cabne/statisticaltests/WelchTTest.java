package ch.uzh.ifi.ce.cabne.statisticaltests;

import org.apache.commons.math3.stat.inference.TTest;

public class WelchTTest extends BaseRankTest<CombiningSampleGatherer> implements UnpairedTest<CombiningSampleGatherer> {
    private final TTest test = new TTest();

    @Override
    public CombiningSampleGatherer supplySampleGatherer() {
        return new CombiningSampleGatherer();
    }

    @Override
    public double signedPValue(CombiningSampleGatherer aSamples, CombiningSampleGatherer bSamples, double significance) {
        double pValue = test.tTest(aSamples, bSamples);
        return Double.compare(aSamples.getMean(), bSamples.getMean()) * Math.max(Math.abs(pValue), Double.MIN_NORMAL);
    }


}
