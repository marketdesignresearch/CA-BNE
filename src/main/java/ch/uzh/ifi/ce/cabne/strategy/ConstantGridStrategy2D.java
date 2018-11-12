package ch.uzh.ifi.ce.cabne.strategy;

import org.apache.commons.math3.linear.RealMatrix;


// Implementation of a strategy s: R^2 --> R^2
// Instead of doing interpolation (like GridStrategy2D), this makes each grid cell share the strategy of
// the lower left corner.
public class ConstantGridStrategy2D extends GridStrategy2D {
    public ConstantGridStrategy2D(RealMatrix leftIntervals, RealMatrix rightIntervals, double maxV1, double maxV2) {
    	super(leftIntervals, rightIntervals, maxV1, maxV2);
    }

    public ConstantGridStrategy2D(RealMatrix leftIntervals, RealMatrix rightIntervals) {
    	this(leftIntervals, rightIntervals,  1.0,  1.0);
    }

    public Double[] getBid(Double[] v) {
        double leftStrategy = computeStrategy(v[0], v[1], leftIntervals);
        double rightStrategy = computeStrategy(v[0], v[1], rightIntervals);
        return new Double[]{leftStrategy, rightStrategy};
    }

    private double computeStrategy(double x, double y, RealMatrix intervals) {
        int rowIndex = (int) (x / rowInterval);
        int columnIndex = (int) (y / columnInterval);
        
        double lowerLeftStrategy = intervals.getEntry(rowIndex, columnIndex);
        return lowerLeftStrategy;
    }
}
