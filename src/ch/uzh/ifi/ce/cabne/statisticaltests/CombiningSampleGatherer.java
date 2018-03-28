package ch.uzh.ifi.ce.cabne.statisticaltests;

import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;

public class CombiningSampleGatherer extends SynchronizedSummaryStatistics implements SimpleSampleGatherer {

    /**
     * 
     */
    private static final long serialVersionUID = -7707068169690055855L;

    @Override
    public int getNumberOfSamples() {
        return (int) super.getN();
    }


}
