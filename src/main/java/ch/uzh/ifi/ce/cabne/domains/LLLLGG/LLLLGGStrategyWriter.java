package ch.uzh.ifi.ce.cabne.domains.LLLLGG;

import java.util.StringJoiner;

import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.strategy.GridStrategy2D;


public class LLLLGGStrategyWriter {
    public String write(GridStrategy2D localstrat, GridStrategy2D globalstrat, int iteration)  {
    	StringBuilder builder = new StringBuilder();
    	builder.append(String.format("\"Iteration\",\"%d\"\n", iteration));
    	builder.append("\"Left\"\n");
    	builder.append(matrixToString(localstrat.getLeftIntervals()));
    	builder.append("\"Right\"\n");
    	builder.append(matrixToString(localstrat.getRightIntervals()));
    	builder.append("\"Global\"\n");
    	builder.append(matrixToString(globalstrat.getLeftIntervals()));
    	return builder.toString();
    }

	public String matrixToString(RealMatrix m) {
		StringBuilder builder = new StringBuilder();
		for (double[] row : m.getData()) {
			StringJoiner joiner = new StringJoiner(",", "", "\n");
			for (int i=0; i<row.length; i++) {
				joiner.add(String.format("\"%7.6f\"", row[i]));
			}
			builder.append(joiner);
		}
		return builder.toString();
	}
}