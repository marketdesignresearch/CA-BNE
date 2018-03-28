package ch.uzh.ifi.ce.cabne.BR;



import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

public class UnivariateIterativeInterpolation implements InterpolationPointGenerator<Double, Double> {
	private boolean convergedEarly = false;

	@Override
    public boolean isConverged(NavigableMap<Double, Double> pointsSoFar, Double min, Double max, int maxPoints) {
		if (convergedEarly) {
			convergedEarly = false;
			return true;
		}
        return pointsSoFar.size() >= maxPoints;
    }

    @Override
    public double[] getNextInterpolationPoints(NavigableMap<Double, Double> pointsSoFar, Double min, Double max, int maxPoints) {
        int numEquidistantPoints = maxPoints / 4 + 1;

        if (pointsSoFar.size() < numEquidistantPoints) {
            int remainingPoints = numEquidistantPoints - pointsSoFar.size();
            return IntStream.range(0, remainingPoints).mapToDouble(i -> i / (remainingPoints - 1d)).map(d -> d * (max - min) + min).toArray();

        }
        //min distance between points such that at most 50% of the points are allocated between two of the base points
        double minDistance=(max-min)*2/(numEquidistantPoints*maxPoints);
        ToDoubleBiFunction<Map.Entry<Double, Double>, Map.Entry<Double, Double>> computeSlope = (e1, e2) -> {
        	return (e2.getValue() - e1.getValue()) / (e2.getKey() - e1.getKey());
        };
        ToDoubleFunction<Map.Entry<Double, Double>> computeSecondDiff = e -> {

            Map.Entry<Double, Double> lowerEntry = pointsSoFar.lowerEntry(e.getKey());
            Map.Entry<Double, Double> higherEntry = pointsSoFar.higherEntry(e.getKey());
            if(e.getKey()-lowerEntry.getKey()<=minDistance||higherEntry.getKey()-e.getKey()<=minDistance){
                return 0;
            }
            double leftSlope = computeSlope.applyAsDouble(e, lowerEntry);
            double rightSlope = computeSlope.applyAsDouble(higherEntry, e);
            //double secondDiff = 2 * (rightSlope - leftSlope) / (higherEntry.getKey() - lowerEntry.getKey());
            double secondDiff = (rightSlope - leftSlope) / 2;
            return Math.abs(secondDiff);
        };
        Map.Entry<Double, Double> midPoint = pointsSoFar.entrySet().stream().skip(1).limit(pointsSoFar.size() - 2).max(Comparator.comparingDouble(computeSecondDiff)).get();

        // we will return a single point.
        double[] singletonArray = new double[1];
        
        Map.Entry<Double, Double> lowerKey = pointsSoFar.lowerEntry(midPoint.getKey());
        Map.Entry<Double, Double> higherKey = pointsSoFar.higherEntry(midPoint.getKey());
        
        /*
        if (computeSecondDiff.applyAsDouble(midPoint) < 0.1) {
        	System.out.println(String.format("converged early (%d points of %d max)",  pointsSoFar.size(), maxPoints));
        	convergedEarly = true;
        	return new double[0];
        }
        */

        double leftDist = midPoint.getKey() - lowerKey.getKey();
        double rightDist = higherKey.getKey() - midPoint.getKey();
        
        double leftSlope = computeSlope.applyAsDouble(lowerKey, midPoint);
        double rightSlope = computeSlope.applyAsDouble(higherKey, midPoint);
        
        // 0.6 is just some value to test, would be 0.5 in absence of floating point errors since we are always taking midpoints and therefore halving distances
        if (leftDist / rightDist < 0.6) {
        	singletonArray[0] = (higherKey.getKey() + midPoint.getKey()) / 2;
        } else if (rightDist / leftDist < 0.6) {
            singletonArray[0] = (lowerKey.getKey() + midPoint.getKey()) / 2;
        } else {
        	// both segments have same size
	        if (Math.abs(leftSlope) >= Math.abs(rightSlope)) {
	            singletonArray[0] = (lowerKey.getKey() + midPoint.getKey()) / 2;
	        } else {
	            singletonArray[0] = (higherKey.getKey() + midPoint.getKey()) / 2;
	        }
        }
        return singletonArray;
    }
}
