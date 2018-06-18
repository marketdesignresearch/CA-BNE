package ch.uzh.ifi.ce.cabne.statisticaltests;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseRankTest<T extends SampleGatherer> implements RankTest<T> {


    /**
     * Tests
     *
     * @return
     */
    public TestFeedback test(double significance, List<T> gatherers, int maxSamples, int maxWinners) {


        T incumbent = gatherers.get(0);
        NavigableSet<Integer> incumbents = new TreeSet<>();
        incumbents.add(0);
        double maxPValue = 0;
        for (int i = 1; i < gatherers.size(); ++i) {
            T challenger = gatherers.get(i);
            double signedPValue = signedPValue(incumbent, challenger, significance);
            if (Math.abs(signedPValue) > significance) {
                incumbents.add(i);
                if (signedPValue < 0) {
                    incumbent = challenger;
                }
            } else if (signedPValue < 0) {
                incumbents.clear();
                incumbents.add(i);
                incumbent = challenger;
            }
            maxPValue = Math.max(maxPValue, Math.abs(signedPValue));

        }
        int[] nextSamples = new int[gatherers.size()];
        if (incumbents.size() == 1) {
            // 1 Winner test passed
            return new TestFeedback(nextSamples, incumbents, maxPValue, TestStatus.PASSED);
        } else if (incumbents.size() <= maxWinners) {

            boolean mergeable = incumbents.stream().mapToInt(i -> i + (gatherers.size() + 1) / 2).map(i -> i % (gatherers.size() + 1)).noneMatch(incumbents::contains);
            if (mergeable && incumbents.stream().map(gatherers::get).mapToInt(T::getNumberOfSamples).allMatch(i -> i >= maxSamples / 10)) {
                return new TestFeedback(nextSamples, incumbents, maxPValue, TestStatus.PASSED);
            }
        }
        // Test Failed
        IntSummaryStatistics usedSamplesSummary = incumbents.stream().map(gatherers::get).mapToInt(T::getNumberOfSamples).summaryStatistics();
        if (usedSamplesSummary.getMin() >= maxSamples) {
            NavigableSet<Integer> actualIncumbents = incumbents.stream().sorted(Comparator.comparing((Integer i) -> gatherers.get(i).getMean()).reversed()).limit(maxWinners)
                    .collect(Collectors.toCollection(TreeSet::new));
            // Sampling limit passed -> test failed
            return new TestFeedback(nextSamples, actualIncumbents, maxPValue, TestStatus.FAILED);
        }
        int totalSamplesSuggestion = Math.min(maxSamples, usedSamplesSummary.getMin() * 2);

        for (int incumbentId : incumbents) {
            nextSamples[incumbentId] = Math.max(0, totalSamplesSuggestion - gatherers.get(incumbentId).getNumberOfSamples());
        }

        return new TestFeedback(nextSamples, incumbents, maxPValue, TestStatus.UNDETERMINED);
    }

    /**
     * @param aSamples
     * @param bSamples
     * @param significance
     * @return statistically compares two sets of sample values. Returns an
     * absolute value of greater 1 iff there exists a significant
     * difference and less then or equal to 1 if not. </br>The returned
     * sign indicates which samples where bigger see {@link Comparator}.
     */
    public abstract double signedPValue(T aSamples, T bSamples, double significance);

}
