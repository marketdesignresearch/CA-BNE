package ch.uzh.ifi.ce.cabne.helpers.distributions;

/**
 * @author Benedikt Bünz (benedikt@cs.stanford.edu)
 */
public interface DensityFunction {
        double density(double... values);
        double marginalDensity(double value);
        double[] sample();

        double[] sample(double value1);
        double[] getUpperBounds();
        String getName();


}
