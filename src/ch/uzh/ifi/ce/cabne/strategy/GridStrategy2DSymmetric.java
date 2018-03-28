package ch.uzh.ifi.ce.cabne.strategy;

import org.apache.commons.math3.linear.RealMatrix;


public class GridStrategy2DSymmetric extends GridStrategy2D {

    public GridStrategy2DSymmetric(RealMatrix intervals, double maxV1, double maxV2) {
        super(intervals, intervals.transpose(), maxV1, maxV2);
    }

    public GridStrategy2DSymmetric(RealMatrix intervals) {
        super(intervals, intervals.transpose());
    }
}
