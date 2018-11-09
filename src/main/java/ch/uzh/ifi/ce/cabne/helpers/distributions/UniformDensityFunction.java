package ch.uzh.ifi.ce.cabne.helpers.distributions;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by buenz on 08.01.16.
 */
public class UniformDensityFunction implements DensityFunction {
    private final double[] upperBounds;
    private final double totalDensity;
    private final String name;

    public UniformDensityFunction(double... upperBounds) {
        this("Uniform", upperBounds);
    }

    public UniformDensityFunction(String name, double... upperBounds) {
        this.name = name;
        this.upperBounds = upperBounds;
        double totalBounds = 1;
        for (double density : upperBounds) {
            totalBounds *= density;
        }
        this.totalDensity = 1 / totalBounds;
    }

    @Override
    public double density(double... values) {
        if (IntStream.range(0, values.length).allMatch(i -> values[i] <= upperBounds[i] && values[i] >= 0)) {
            return totalDensity;
        } else {
            return 0;
        }
    }

    public double marginalDensity(double value) {

        return value<=upperBounds[0]&&value>=0? 1d / upperBounds[0]:0;
    }

    @Override
    public double[] getUpperBounds() {
        return upperBounds;
    }

    @Override
    public double[] sample() {
        return Arrays.stream(upperBounds).map(d -> d * Math.random()).toArray();
    }

    @Override
    public double[] sample(double value1) {
        return Arrays.stream(upperBounds).skip(1).map(d -> d * Math.random()).toArray();
    }

    @Override
    public String getName() {
        return name;
    }
}
