package ch.uzh.ifi.ce.cabne.domains.FirstPriceLLG;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.helpers.distributions.DensityFunction;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.strategy.UnivariatePWLStrategy;

import java.util.Iterator;
import java.util.List;


public class FirstPriceLLGDensitySampler extends BidSampler<Double, Double> {
    private DensityFunction densityFunction;

    public FirstPriceLLGDensitySampler(BNESolverContext<Double, Double> context, DensityFunction densityFunction) {
        super(context);
        this.densityFunction = densityFunction;
    }

    public Iterator<Sample> conditionalBidIterator(int i, Double v, Double b, List<Strategy<Double, Double>> s) {
        if (i == 2) {

            Iterator<double[]> rngiter = context.getRng(2).nextVectorIterator();
            UnivariatePWLStrategy slocal0 = (UnivariatePWLStrategy) s.get(0);
            UnivariatePWLStrategy slocal1 = (UnivariatePWLStrategy) s.get(1);

            Iterator<Sample> it = new Iterator<Sample>() {
                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Sample next() {
                    double[] r = rngiter.next();
                    Double result[] = new Double[3];

                    // bids of local players
                    double density = 1.0 / slocal0.getMaxValue() / slocal1.getMaxValue();

                    //double maxlocalbid0 = Math.min(b, slocal0.getBid(slocal0.getMaxValue()));
                    //double maxlocalvalue0 = slocal0.invert(maxlocalbid0);
                    double maxlocalvalue0 = slocal0.getMaxValue();
                    density *= maxlocalvalue0 / slocal0.getMaxValue();
                    result[0] = slocal0.getBid(r[0] * maxlocalvalue0);

                    double maxlocalbid1 = Math.max(0.0, Math.min(b - result[0], slocal1.getBid(slocal1.getMaxValue())));
                    double maxlocalvalue1 = slocal1.invert(maxlocalbid1);
                    density *= maxlocalvalue1 / slocal1.getMaxValue();
                    result[1] = slocal1.getBid(r[1] * maxlocalvalue1);

                    result[2] = b;

                    return new Sample(density, result);
                }
            };
            return it;
        }

        final int localopponent = (i + 1) % 2;
        Iterator<double[]> rngiter = context.getRng(2).nextVectorIterator();
        Strategy<Double, Double> slocal = s.get(localopponent);
        UnivariatePWLStrategy sglobal = (UnivariatePWLStrategy) s.get(2);

        Iterator<Sample> it = new Iterator<Sample>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Sample next() {
                double[] r = rngiter.next();
                Double result[] = new Double[3];

                result[i] = b;
                result[localopponent] = slocal.getBid(r[0] * slocal.getMaxValue());

                double maxglobalbid = Math.min(b + result[localopponent], sglobal.getBid(sglobal.getMaxValue()));
                double maxglobalvalue = sglobal.invert(maxglobalbid);
                double density = maxglobalvalue / sglobal.getMaxValue() / slocal.getMaxValue() / sglobal.getMaxValue();

                result[2] = sglobal.getBid(r[1] * maxglobalvalue);

                return new Sample(density, result);
            }
        };
        return it;
    }
}
