package ch.uzh.ifi.ce.cabne.helpers.distributions;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.text.DecimalFormat;

/**
 * Created by buenz on 08.01.16.
 */
public class BetaCommonsDensityFunctions extends CommonsDensityFunctions {
    public BetaCommonsDensityFunctions(BetaDistribution dist) {
        super("betaa"+new DecimalFormat("#.##").format(dist.getAlpha())+"b"+new DecimalFormat("#.##").format(dist.getBeta()) ,dist,dist,new UniformRealDistribution(0,2));
    }
    public BetaCommonsDensityFunctions(BetaEpsilonRealDist dist) {
        super("betaa"+new DecimalFormat("#.##").format(dist.getAlpha())+"b"+new DecimalFormat("#.##").format(dist.getBeta())+"eps"+new DecimalFormat("0").format(-Math.log10(dist.getEpsilon())),dist,dist,new UniformRealDistribution(0,2));
    }
    public BetaCommonsDensityFunctions(BetaEpsilonRealDist dist, double scale) {
        super("betaa"+new DecimalFormat("#.##").format(dist.getAlpha())+"b"+new DecimalFormat("#.##").format(dist.getBeta())+"eps"+new DecimalFormat("0").format(-Math.log10(dist.getEpsilon()))+"s"+new DecimalFormat("#.##").format(scale),dist,dist,new UniformRealDistribution(0,2*scale));
    }
    public BetaCommonsDensityFunctions(BetaEpsilonRealDist dist, double scale, double epsilon, String suffix) {
        super(("betaa"+new DecimalFormat("#.##").format(dist.getAlpha())+"b"+new DecimalFormat("#.##").format(dist.getBeta())+"eps"+new DecimalFormat("0").format(-Math.log10(dist.getEpsilon()))+"s"+new DecimalFormat("#.##").format(scale)+suffix),dist,dist,new UniformRealDistribution(0,2*scale));
    }

}

