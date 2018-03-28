package ch.uzh.ifi.ce.cabne.strategy;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

// Implementation of a strategy s: R^2 --> R^2
// It is implemented by having a fixed grid of control points, with interpolation performed between them.
// The interpolation method splits each rectangle into 2 triangles, then interpolates the strategy linearly over each triangle.
// An alternative would be using bilinear interpolation, which is not linear but smooth across the rectangle.
public class GridStrategy2D implements Strategy<Double[], Double[]> {
	protected RealMatrix leftIntervals;
    protected RealMatrix rightIntervals;

    protected double rowInterval;
    protected double columnInterval;
    
    private Double[] maxValue;
    
    public GridStrategy2D(RealMatrix leftIntervals, RealMatrix rightIntervals, double maxV1, double maxV2) {
    	this.leftIntervals = leftIntervals;
        this.rightIntervals = rightIntervals;
        this.rowInterval = maxV1 / (leftIntervals.getRowDimension() - 1);
        this.columnInterval = maxV2 / (leftIntervals.getColumnDimension() - 1);
        this.maxValue = new Double[] {maxV1, maxV2};
    }
    
    public GridStrategy2D(RealMatrix leftIntervals, RealMatrix rightIntervals) {
    	this(leftIntervals, rightIntervals,  1.0,  1.0);
    }

    public Double[] getBid(Double[] v) {
        double leftStrategy = computeStrategy(v[0], v[1], leftIntervals);
        double rightStrategy = computeStrategy(v[0],v[1], rightIntervals);
        return new Double[]{leftStrategy, rightStrategy};
    }

    private double computeStrategy(double x, double y, RealMatrix intervals) {
    	// TODO: this needs to deal with out-of-bounds queries
    	
        int rowIndex = (int) (x / rowInterval);
        int columnIndex = (int) (y / columnInterval);
        int upperRow = Math.min(rowIndex + 1, intervals.getRowDimension() - 1);
        int upperColumn = Math.min(columnIndex + 1, intervals.getColumnDimension() - 1);
        
        double lowerLeftStrategy = intervals.getEntry(rowIndex, columnIndex);
        double upperRightStrategy = intervals.getEntry(upperRow, upperColumn);
        double rowDiff = x - rowIndex * rowInterval;
        double columnDiff = y - columnIndex * columnInterval;
        double xComponentStrategy = rowDiff / rowInterval;
        double yComponentStrategy = columnDiff / columnInterval;
        if (rowDiff > columnDiff) {
            double cornerStrategy = intervals.getEntry(upperRow, columnIndex);
            xComponentStrategy *= (cornerStrategy - lowerLeftStrategy);
            yComponentStrategy *= (upperRightStrategy - cornerStrategy);
        } else {
            double cornerStrategy = intervals.getEntry(rowIndex, upperColumn);
            xComponentStrategy *= (upperRightStrategy - cornerStrategy);
            yComponentStrategy *= (cornerStrategy - lowerLeftStrategy);
        }

        return lowerLeftStrategy + xComponentStrategy + yComponentStrategy;
    }

    public static GridStrategy2D makeTruthful(double maxV1, double maxV2) {
    	double[][] d = new double[][]{
    		new double[]{0.0, 0.0},
    		new double[]{maxV1, maxV2}
    	};
    	
    	RealMatrix left = new Array2DRowRealMatrix(d);
    	return new GridStrategy2DSymmetric(left, maxV1, maxV2);
    }    
    
    public static GridStrategy2D makeShadingStrategy(double factor, double maxV1, double maxV2) {
    	double[][] d = new double[][]{
    			new double[]{0.0, 0.0},
    			new double[]{factor * maxV1, factor * maxV2}
    	};
    	RealMatrix left = new Array2DRowRealMatrix(d);
    	return new GridStrategy2DSymmetric(left, maxV1, maxV2);
    }
    
    public RealMatrix getLeftIntervals() {
		return leftIntervals;
	}

	public RealMatrix getRightIntervals() {
		return rightIntervals;
	}

	@Override
	public Double[] getMaxValue() {
		return maxValue;
	}
}
