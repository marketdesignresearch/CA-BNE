package ch.uzh.ifi.ce.cabne.examples;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.ce.cabne.BR.AdaptivePWLBRCalculator;
import ch.uzh.ifi.ce.cabne.BR.ExactUnivariateVerifier;
import ch.uzh.ifi.ce.cabne.BR.HeuristicUnivariateVerifier;
import ch.uzh.ifi.ce.cabne.BR.PWLBRCalculator;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithm;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithmCallback;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.LLG.LLGSampler;
import ch.uzh.ifi.ce.cabne.domains.LLG.Quadratic;
import ch.uzh.ifi.ce.cabne.integration.MCIntegrator;
import ch.uzh.ifi.ce.cabne.pointwiseBR.PatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.UnivariatePattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.UnivariateDampenedUpdateRule;
import ch.uzh.ifi.ce.cabne.randomsampling.CommonRandomGenerator;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.strategy.UnivariatePWLStrategy;


public class LLGQuadratic {
	
	public static void main(String[] args) throws InterruptedException, IOException {		

		// create context and read config
		BNESolverContext<Double, Double> context = new BNESolverContext<>();
		String configfile = args[0];
		context.parseConfig(configfile);
		
		// initialize all algorithm pieces
		context.setOptimizer(new PatternSearch<Double, Double>(context, new UnivariatePattern()));
		context.setIntegrator(new MCIntegrator<Double, Double>(context));
		context.setRng(2, new CommonRandomGenerator(2));
		context.setUpdateRule(new UnivariateDampenedUpdateRule(0.2, 0.7, 0.5 / context.getDoubleParameter("epsilon"), true));
		context.setBRC(new AdaptivePWLBRCalculator(context));
		context.setOuterBRC(new PWLBRCalculator(context));
		context.setVerifier(new ExactUnivariateVerifier(context));
		//context.setVerifier(new HeuristicUnivariateVerifier(context));
		
		// instanciate auction setting
		context.setMechanism(new Quadratic());
		context.setSampler(new LLGSampler(context));
		
		

		BNEAlgorithm<Double, Double> bneAlgo = new BNEAlgorithm<>(3, context);
		
		// add bidders, giving each an initial strategy and telling the algorithm which ones to update.
		// bidder 0 (first local bidder) does a best response in each iteration
		// bidder 1 (second local bidder) plays symmetrically to bidder 0
		// bidder 2 (global bidder) plays truthful and thus doesn't update his strategy.
		bneAlgo.setInitialStrategy(0, UnivariatePWLStrategy.makeTruthful(0.0, 1.0));
		bneAlgo.setInitialStrategy(2, UnivariatePWLStrategy.makeTruthful(0.0, 2.0));
		bneAlgo.makeBidderSymmetric(1, 0);
		bneAlgo.makeBidderNonUpdating(2);
		
		// create callback that prints out first local player's strategy after each iteration
		BNEAlgorithmCallback<Double, Double> callback = new BNEAlgorithmCallback<Double, Double>() {
			@Override
			public void afterIteration(int iteration, BNEAlgorithm.IterationType type, List<Strategy<Double, Double>> strategies, double epsilon) {
				// print out strategy
				StringBuilder builder = new StringBuilder();
				builder.append(String.format("%2d", iteration));
				builder.append(String.format(" %7.6f  ", epsilon));
				
				// cast s to UnivariatePWLStrategy to get access to underlying data structure.
				UnivariatePWLStrategy sPWL = (UnivariatePWLStrategy) strategies.get(0);
				for (Map.Entry<Double, Double> e : sPWL.getData().entrySet()) {
					builder.append(String.format("%7.6f",e.getKey()));
					builder.append(" ");
					builder.append(String.format("%7.6f",e.getValue()));
					builder.append("  ");
				}
				
				// alternatively, just sample the strategy on a regular grid.
				/*
				for (int i=0; i<=100; i++) {
					double v = s.getMaxValue() * i / ((double) gridSize);
					builder.append(String.format("%7.6f",v));
					builder.append(" ");
					builder.append(String.format("%7.6f",s.getBid(v)));
					builder.append("  ");
				}
				*/
				System.out.println(builder.toString());
			}
		};
		bneAlgo.setCallback(callback);
		
		bneAlgo.run();
    }
}
