package ch.uzh.ifi.ce.cabne.helpers.distributions;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.linear.*;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Created by buenz on 08.01.16.
 */
public class GaussianCopulaDensity implements DensityFunction {
    private final RealMatrix inverseMinIdentity;
    private final double inverseSqrtDeterminant;
    private NormalDistribution sd = new NormalDistribution();
    private final RealMatrix lowerCholesky;
    private final static double EPSILON = 1e-8;
    private final static double ZERO = 1e-16;

    private final String name;
    private final RealDistribution[] densityFunctions;
    private final double[] upperBounds;

    public GaussianCopulaDensity(RealMatrix covariance, String name) {
        this(covariance, name, new UniformRealDistribution(0, 1), new UniformRealDistribution(0, 1), new UniformRealDistribution(0, 2));
    }

    public GaussianCopulaDensity(RealMatrix covariance, String name, RealDistribution... densityFunctions) {
        if (!covariance.isSquare() || covariance.getRowDimension() != densityFunctions.length) {
            throw new IllegalArgumentException("Wrong matrix dimensions");
        }
        this.name = name;
        LUDecomposition decomposition = new LUDecomposition(covariance);
        RealMatrix inverse = decomposition.getSolver().getInverse();
        inverseMinIdentity = inverse.subtract(MatrixUtils.createRealIdentityMatrix(covariance.getRowDimension()));
        inverseSqrtDeterminant = 1 / Math.sqrt(decomposition.getDeterminant());
        lowerCholesky = new CholeskyDecomposition(covariance).getLT();
        this.densityFunctions = densityFunctions;
        this.upperBounds = Arrays.stream(densityFunctions).mapToDouble(RealDistribution::getSupportUpperBound).toArray();
    }

    @Override
    public double density(double... values) {


        DoubleStream mappedValues = IntStream.range(0, densityFunctions.length).mapToDouble(i -> densityFunctions[i].cumulativeProbability(values[i]));
        double[] probabilities = mappedValues.map(v -> v < ZERO ? EPSILON : v).map(v -> v == 1 ? 1 - EPSILON : v).map(sd::inverseCumulativeProbability).toArray();
        RealVector vector = new ArrayRealVector(probabilities);
        double result = inverseMinIdentity.preMultiply(vector).dotProduct(vector);
        if (Double.isInfinite(result) && Math.signum(result) > 0) {
            return 0d;
        }

        if (Double.isNaN(result)) {
            throw new IllegalArgumentException("Does not happen");
           /*
            if (inverseSqrtDeterminant == 1d) {
                return 1d;
            }
            if (Arrays.stream(mappedValues).anyMatch(d -> d * d == d)) {
                return 0d;
            }
            */
        }
        double densityProduct = IntStream.range(0, densityFunctions.length).mapToDouble(i -> densityFunctions[i].density(values[i])).reduce(1d, (d1, d2) -> d1 * d2);
        double density = inverseSqrtDeterminant * Math.exp(-result / 2) * densityProduct;
        if (!Double.isFinite(density)) {
            throw new IllegalArgumentException(String.format("The density at %s is not finite", values[0]));
        }
        return density;


    }

    @Override
    public double marginalDensity(double value) {
        return densityFunctions[0].density(value);
    }

    @Override
    public double[] sample() {
        double z[] = sd.sample(inverseMinIdentity.getColumnDimension());
        double[] x = lowerCholesky.preMultiply(z);
        double[] result = Arrays.stream(x).map(sd::cumulativeProbability).toArray();
        result[result.length - 1] *= 2;
        return result;
    }

    @Override
    public double[] sample(double value1) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getName() {
        return "Gaussian" + name;
    }

    @Override
    public double[] getUpperBounds() {
        return upperBounds;
    }
}
