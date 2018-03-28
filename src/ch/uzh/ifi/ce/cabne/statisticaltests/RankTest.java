package ch.uzh.ifi.ce.cabne.statisticaltests;

import java.util.List;

public interface RankTest<T extends SampleGatherer> {

    T supplySampleGatherer();

    /**
     * Tests
     *
     * @return
     */
    TestFeedback test(double significance, List<T> gatherers, int maxSamples, int maxWinners);

}
