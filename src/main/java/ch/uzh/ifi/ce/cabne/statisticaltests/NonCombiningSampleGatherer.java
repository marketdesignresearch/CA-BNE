package ch.uzh.ifi.ce.cabne.statisticaltests;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class NonCombiningSampleGatherer implements SimpleSampleGatherer {
    private final List<Double> seenSamples = new LinkedList<>();
    @Override
    public void addValue(double sample) {
        seenSamples.add(sample);
    }

    @Override
    public double getMean() {
        return seenSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    @Override
    public int getNumberOfSamples() {
        return seenSamples.size();
    }

    public List<Double> getSeenSamples() {
        return seenSamples;
    }
    @Override
    public double[] getMeanDiff(SampleGatherer other) {
        if(other instanceof  NonCombiningSampleGatherer) {
            NonCombiningSampleGatherer otherGatherer= (NonCombiningSampleGatherer) other;
            int usableSamples =Math.min(getNumberOfSamples(),other.getNumberOfSamples());
            Iterator<Double> aIterator=getSeenSamples().iterator();
            Iterator<Double> bIterator=otherGatherer.getSeenSamples().iterator();
            double [] means=new double[2];
            while (aIterator.hasNext()&&bIterator.hasNext()){
                means[0]+=aIterator.next();
                means[1]+=bIterator.next();
            }
            means[0]/=usableSamples;
            means[1]/=usableSamples;
            return means;
        }else {
            return  new double[]{getMean(),other.getMean()};
        }
        }

    @Override
    public double getStandardDeviation() {
        //TODO: Actual implementation
        return -1;

    }

    
}
