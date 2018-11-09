package ch.uzh.ifi.ce.cabne.helpers.distributions;

import org.apache.commons.math3.distribution.BetaDistribution;

@SuppressWarnings("serial")
public class BetaEpsilonRealDist extends BetaDistribution {
    private final double epsilon;
    private final double cdfCorrection;
    private double epsilonCDF;

    public BetaEpsilonRealDist(double alpha, double beta, double epsilon) {
        super(alpha, beta);
        this.epsilon = epsilon;
        cdfCorrection = super.cumulativeProbability(1 - epsilon) - super.cumulativeProbability(epsilon);
        epsilonCDF = super.cumulativeProbability(epsilon);

    }

    @Override
    public double density(double x) {
        if (x < 0 || x > 1) {
            return 0;
        }
        return ( super.density(x * (1 - 2 * epsilon) + epsilon) )*(1-2*epsilon)/ (cdfCorrection);
    }


    @Override
    public double cumulativeProbability(double x) {
        if(x<0){
            return 0;
        }
        if(x>1){
            return 1;
        }
        return (super.cumulativeProbability(x * (1 - 2 * epsilon) + epsilon)- epsilonCDF)/ cdfCorrection;
    }

    @Override
    public double sample() {
        double sample=super.sample();
        while (sample<epsilon||sample>1-epsilon){
            sample=super.sample();
        }
        return (sample-epsilon)/ (1 - 2 * epsilon) ;
    }

    public double getEpsilon() {
        return epsilon;
    }
}
