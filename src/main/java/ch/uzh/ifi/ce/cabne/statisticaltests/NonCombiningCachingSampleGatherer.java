package ch.uzh.ifi.ce.cabne.statisticaltests;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;


public class NonCombiningCachingSampleGatherer extends NonCombiningSampleGatherer implements StatisticalSummary {
    private final SummaryStatistics summaryStatistics = new SummaryStatistics();

    @Override
    public void addValue(double sample) {
        super.addValue(sample);
        summaryStatistics.addValue(sample);
    }

    @Override
    public double getMean() {
        return summaryStatistics.getMean();
    }

    @Override
    public double getVariance() {
        return summaryStatistics.getVariance();
    }

    @Override
    public double getStandardDeviation() {
        return summaryStatistics.getStandardDeviation();
    }

    @Override
    public double getMax() {
        return summaryStatistics.getMax();
    }

    @Override
    public double getMin() {
        return summaryStatistics.getMin();
    }

    @Override
    public long getN() {
        return summaryStatistics.getN();
    }

    @Override
    public double getSum() {
        return summaryStatistics.getSum();
    }


    /*
    @Override
    public String toString() {
        return MessageFormat.format("NonCombiningCachingSampleGatherer[mean={0,number,0.#####},samples={1,number,#,###},stdErr={2,number,0.###E0}]", getMean(), getN(), getStandardError());
    }
    */
}
