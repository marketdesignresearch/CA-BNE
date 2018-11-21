package ch.uzh.ifi.ce.cabne.examples;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.uzh.ifi.ce.cabne.BR.AdaptivePWLBRCalculator;
import ch.uzh.ifi.ce.cabne.BR.PWLBRCalculator;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithm;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithmCallback;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.FirstPriceLLG.FirstPrice;
import ch.uzh.ifi.ce.cabne.domains.FirstPriceLLG.FirstPriceLLGSampler;
import ch.uzh.ifi.ce.cabne.integration.MCIntegrator;
import ch.uzh.ifi.ce.cabne.pointwiseBR.PatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.UnivariatePattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.UnivariateDampenedUpdateRule;
import ch.uzh.ifi.ce.cabne.randomsampling.CommonRandomGenerator;
import ch.uzh.ifi.ce.cabne.strategy.UnivariatePWLStrategy;
import ch.uzh.ifi.ce.cabne.verification.BoundingVerifier1D;


public class LLGFirstPrice {
	
	public static void main(String[] args) throws InterruptedException, IOException {		

		// create context and read config
		BNESolverContext<Double, Double> context = new BNESolverContext<>();
		String configfile = args[0];
		context.parseConfig(configfile);
		
		// initialize all algorithm pieces
		context.setOptimizer(new PatternSearch<>(context, new UnivariatePattern()));
		context.setIntegrator(new MCIntegrator<>(context));
		context.setRng(2, new CommonRandomGenerator(2));
		context.setUpdateRule(new UnivariateDampenedUpdateRule(0.2, 0.7, 0.5 / context.getDoubleParameter("epsilon"), true));
		context.setBRC(new AdaptivePWLBRCalculator(context));
		context.setOuterBRC(new PWLBRCalculator(context));
		context.setVerifier(new BoundingVerifier1D(context));
		
		// instanciate auction setting
		context.setMechanism(new FirstPrice());
		context.setSampler(new FirstPriceLLGSampler(context));
		
		BNEAlgorithm<Double, Double> bneAlgo = new BNEAlgorithm<>(3, context);
		
		// add bidders
		bneAlgo.setInitialStrategy(0, UnivariatePWLStrategy.makeTruthful(0.0, 1.0));
		bneAlgo.setInitialStrategy(2, UnivariatePWLStrategy.makeTruthful(0.0, 2.0));
		bneAlgo.makeBidderSymmetric(1, 0);
		
		// create callback that prints out the local and global players' strategies after each iteration, and also forces the strategies to be monotone
		BNEAlgorithmCallback<Double, Double> callback = (iteration, type, strategies, epsilon) -> {
            // print out strategy
            StringBuilder builder = new StringBuilder();
            builder.append(String.format("%2d", iteration));
            builder.append(String.format(" %7.6f  ", epsilon));

            int ngridpoints = 1000;
            for (int i=0; i<=ngridpoints/2; i++) {
                double v = strategies.get(2).getMaxValue() * i / ngridpoints;
                builder.append(String.format("%5.4f",v));
                builder.append(" ");
                builder.append(String.format("%5.4f", strategies.get(0).getBid(v)));
                builder.append(" ");
                builder.append(String.format("%5.4f", strategies.get(2).getBid(v)));
                builder.append("  ");
            }
            for (int i=ngridpoints/2; i<=ngridpoints; i++) {
                double v = strategies.get(2).getMaxValue() * i / ngridpoints;
                builder.append(String.format("%5.4f",v));
                builder.append(" ");
                builder.append("0.0000");
                builder.append(" ");
                builder.append(String.format("%5.4f", strategies.get(2).getBid(v)));
                builder.append("  ");
            }
            System.out.println(builder.toString());

            // make the strategy monotone, so it can be inverted for importance sampling
            for (int i=0; i<3; i+=2) {
                UnivariatePWLStrategy s = (UnivariatePWLStrategy) strategies.get(i);
                SortedMap<Double, Double> data = s.getData();
                SortedMap<Double, Double> newdata = new TreeMap<>();

                double previousBid = 0.0;
                for (Map.Entry<Double, Double> e : data.entrySet()) {
                    double v = e.getKey();
                    double bid = Math.max(previousBid + 1e-6, e.getValue());
                    newdata.put(v, bid);
                    previousBid = bid;
                }
                strategies.set(i, new UnivariatePWLStrategy(newdata));
            }
            strategies.set(1, strategies.get(0));
        };
		bneAlgo.setCallback(callback);
		
		BNEAlgorithm.Result<Double, Double> result;
		result = bneAlgo.run();
    }
}
