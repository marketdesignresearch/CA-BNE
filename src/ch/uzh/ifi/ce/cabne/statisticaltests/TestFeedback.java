package ch.uzh.ifi.ce.cabne.statisticaltests;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.NavigableSet;

public class TestFeedback {
    private final int[] nextSamples;
    private final TestStatus status;
    private final NavigableSet<Integer> maxIndices;
    private  final double maxPValue;
    public TestFeedback(int[] nextSamples, NavigableSet<Integer> maxIndices,double maxPValue, TestStatus status) {
        this.nextSamples = nextSamples;
        this.status = status;
        this.maxIndices = maxIndices;
        this.maxPValue=maxPValue;
    }

    public int[] getNextSamples() {
        return nextSamples;
    }


    public TestStatus getStatus() {
        return status;
    }

    public NavigableSet<Integer> getMaxIndices() {
        return maxIndices;
    }

    public double getMaxPValue() {
        return maxPValue;
    }

    @Override
    public String toString() {
        int[] relevantNextSamples = maxIndices.stream().mapToInt(i -> nextSamples[i]).toArray();

        return MessageFormat.format("TestFeedback[status={0}, nextSamples={1},  maxIndices={2}, maxPValue={3}", status, Arrays.toString(relevantNextSamples),
                maxIndices,maxPValue);
    }
}
