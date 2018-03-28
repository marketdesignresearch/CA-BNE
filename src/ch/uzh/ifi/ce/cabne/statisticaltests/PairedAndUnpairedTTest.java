package ch.uzh.ifi.ce.cabne.statisticaltests;

import org.apache.commons.math3.stat.inference.TTest;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class PairedAndUnpairedTTest extends BaseRankTest<NonCombiningCachingSampleGatherer> implements RankTest<NonCombiningCachingSampleGatherer> {
    private final TTest ttest = new TTest();
    //private static final Logger LOGGER = LoggerFactory.getLogger(PairedAndUnpairedTTest.class);

    @Override
    public NonCombiningCachingSampleGatherer supplySampleGatherer() {
        return new NonCombiningCachingSampleGatherer();
    }

    @Override
    public double signedPValue(NonCombiningCachingSampleGatherer aSamples, NonCombiningCachingSampleGatherer bSamples, double significance) {
        double pValue = ttest.tTest(aSamples, bSamples) / 2;
        double aMean = aSamples.getMean();
        double bMean = bSamples.getMean();
        if (pValue > significance) {
            int usableSamples = Math.min(aSamples.getNumberOfSamples(), bSamples.getNumberOfSamples());
            double[] aSampleArray = aSamples.getSeenSamples().stream().limit(usableSamples).mapToDouble(Double::doubleValue).toArray();
            double[] bSampleArray = bSamples.getSeenSamples().stream().limit(usableSamples).mapToDouble(Double::doubleValue).toArray();
            
            double pairedPValue = ttest.pairedTTest(aSampleArray, bSampleArray) / 2;
            //LOGGER.debug("Paired pValue {} unpaired pValue {} means {} and {} with samples {} and {}", pairedPValue, pValue, aMean, bMean, aSamples.getN(), bSamples.getN());
            if (pairedPValue < pValue) {
                double[] meanDiffs = aSamples.getMeanDiff(bSamples);
                aMean = meanDiffs[0];
                bMean = meanDiffs[1];
                pValue = pairedPValue;
            }
        }
        if (Double.compare(aMean, bMean) == Double.compare(aSamples.getMean(), bSamples.getMean())) {
            return Double.compare(aMean, bMean) * Math.max(Math.abs(pValue), Double.MIN_NORMAL);
        } else {
            return Double.compare(aMean, bMean) * (significance + .1);
        }
    }

}
