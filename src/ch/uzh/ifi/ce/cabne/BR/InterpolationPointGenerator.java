package ch.uzh.ifi.ce.cabne.BR;

import java.util.NavigableMap;

public interface InterpolationPointGenerator<Value, Bid> {
    default boolean isConverged(NavigableMap<Value, Bid> pointsSoFar, Value min, Value max, int maxPoints) {
        return pointsSoFar.size() >= maxPoints;
    }

   double[] getNextInterpolationPoints(NavigableMap<Value, Bid> pointsSoFar, Value min, Value max, int maxPoints);
}
