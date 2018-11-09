package ch.uzh.ifi.ce.cabne.helpers.distributions;

import org.apache.commons.math3.distribution.RealDistribution;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by buenz on 08.01.16.
 */
public class CommonsDensityFunctions implements DensityFunction {
    private final RealDistribution[] distributions;
    private final String name;
    private final double[] upperBounds;

    public CommonsDensityFunctions(String name, RealDistribution... distributions) {
        this.distributions = distributions;
        this.name = name;
        upperBounds = Arrays.stream(distributions).mapToDouble(RealDistribution::getSupportUpperBound).toArray();
    }

    @Override
    public double density(double... values) {

        return IntStream.range(0, values.length).mapToDouble(i -> distributions[i].density(values[i])).reduce(1, (d1, d2) -> d1 * d2);
    }

    @Override
    public double marginalDensity(double value) {

        return distributions[0].density(value);
    }

    @Override
    public double[] sample() {
        return Arrays.stream(distributions).mapToDouble(RealDistribution::sample).toArray();
    }

    @Override
    public double[] sample(double value1) {
        return Arrays.stream(distributions).skip(1).mapToDouble(RealDistribution::sample).toArray();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double[] getUpperBounds() {
        return upperBounds;
    }
}
