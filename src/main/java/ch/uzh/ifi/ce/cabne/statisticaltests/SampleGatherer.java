package ch.uzh.ifi.ce.cabne.statisticaltests;

public interface SampleGatherer {


    double getMean();

    default double[] getMeanDiff(SampleGatherer otherGatherer) {
        double[] meanDiffs = {getMean(), otherGatherer.getMean()};
        return meanDiffs;
    }

    double getStandardDeviation();

    default double getStandardError() {
        return getStandardDeviation() / Math.sqrt(getNumberOfSamples());
    }

    int getNumberOfSamples();
}
