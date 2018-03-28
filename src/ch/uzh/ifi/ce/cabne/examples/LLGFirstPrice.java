package ch.uzh.ifi.ce.cabne.examples;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.uzh.ifi.ce.cabne.BR.AdaptivePWLBRCalculator;
import ch.uzh.ifi.ce.cabne.BR.ExactUnivariateVerifier;
import ch.uzh.ifi.ce.cabne.BR.HeuristicUnivariateVerifier;
import ch.uzh.ifi.ce.cabne.BR.PWLBRCalculator;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithm;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithmCallback;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.Mechanism;
import ch.uzh.ifi.ce.cabne.domains.FirstPriceLLG.FirstPrice;
import ch.uzh.ifi.ce.cabne.domains.FirstPriceLLG.FirstPriceLLGSampler;
import ch.uzh.ifi.ce.cabne.integration.MCIntegrator;
import ch.uzh.ifi.ce.cabne.pointwiseBR.PatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.UnivariatePattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.UnivariateDampenedUpdateRule;
import ch.uzh.ifi.ce.cabne.randomsampling.CommonRandomGenerator;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.strategy.UnivariatePWLStrategy;


public class LLGFirstPrice {
	
	public static void main(String[] args) throws InterruptedException, IOException {		

		String configfile = args[0];
		
		Mechanism<Double, Double> mechanism = new FirstPrice();
				
		// create context and read config
		BNESolverContext<Double, Double> context = new BNESolverContext<>();
		context.parseConfig(configfile);
		
		double targetepsilon = context.getDoubleParameter("epsilon");

		
		// initialize all algorithm pieces
		PatternSearch<Double, Double> patternSearch = new PatternSearch<>(context, new UnivariatePattern()); 
		context.setOptimizer(patternSearch);
		context.setIntegrator(new MCIntegrator<Double, Double>(context));
		context.setMechanism(mechanism);
		context.setRng(2, new CommonRandomGenerator(2));
		context.setSampler(new FirstPriceLLGSampler(context));
		context.setUpdateRule(new UnivariateDampenedUpdateRule(0.2, 0.7, 0.5 / targetepsilon, true));
		context.setBRC(new AdaptivePWLBRCalculator(context));
		context.setOuterBRC(new PWLBRCalculator(context));
		context.setVerifier(new HeuristicUnivariateVerifier(context));
		//context.setVerifier(new ExactUnivariateVerifier(context));
		
		// create callback that prints out player 0's strategy after each iteration
		BNEAlgorithmCallback<Double, Double> callback = new BNEAlgorithmCallback<Double, Double>() {
			@Override
			public void afterIteration(int iteration, BNEAlgorithm.IterationType type, List<Strategy<Double, Double>> strategies, double epsilon) {	
				
				
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

				// Cool down pattern over time, but only do this in inner loop! 
				// (Otherwise verification will be too optimistic)
				double temperature = Math.pow(0.7, Math.max(0.0, iteration - 5));
				if (type != BNEAlgorithm.IterationType.INNER) {
					temperature = 1.0;
				}
				patternSearch.setInitialScale(0.01+0.99*temperature);
				
				
				// smooth the strategy
				/*
				for (int i=0; i<3; i+=2) {
					UnivariatePWLStrategy s = (UnivariatePWLStrategy) strategies.get(i);
					SortedMap<Double, Double> data = s.getData();
					SortedMap<Double, Double> newdata = new TreeMap<>();
					
					for (Map.Entry<Double, Double> e : data.entrySet()) {
						double v = e.getKey();
						double bid = 0.0;
						for (int offset = -2; offset <=2; offset++) {
							bid += s.getBid(v + 0.1*offset*temperature);
						}
						bid /= 5.0;
						newdata.put(v, Math.max(0.0, bid));
					}

					strategies.set(i, new UnivariatePWLStrategy(newdata));
				}
				strategies.set(1, strategies.get(0));
				*/

				// smooth the strategy (another approach)
				/*
				Random random = new Random();
				
				for (int i=0; i<3; i+=2) {
					UnivariatePWLStrategy s = (UnivariatePWLStrategy) strategies.get(i);
					SortedMap<Double, Double> data = s.getData();
					SortedMap<Double, Double> newdata = new TreeMap<>();
					
					for (Map.Entry<Double, Double> e : data.entrySet()) {
						double v = e.getKey();
						double bid = e.getValue() + 0.00001 * temperature * random.nextGaussian();
						newdata.put(v, Math.max(0.0, bid));
					}
					
					strategies.set(i, new UnivariatePWLStrategy(newdata));
				}
				strategies.set(1, strategies.get(0));
				*/
				
				
				// make the strategy monotone
				
				for (int i=0; i<3; i+=2) {
					UnivariatePWLStrategy s = (UnivariatePWLStrategy) strategies.get(i);
					SortedMap<Double, Double> data = s.getData();
					SortedMap<Double, Double> newdata = new TreeMap<>();
					
					double previousBid = 0.0;
					for (Map.Entry<Double, Double> e : data.entrySet()) {
						double v = e.getKey();
						double bid = Math.max(previousBid + 0.0001, e.getValue());
						newdata.put(v, Math.max(0.0, bid));
						previousBid = bid;
					}
					
					strategies.set(i, new UnivariatePWLStrategy(newdata));
				}
				strategies.set(1, strategies.get(0));
				
				
				//System.out.println(context.integrator.computeExpectedUtility(2, 1.9, 1.0, strategies));
				//System.out.println(context.integrator.computeExpectedUtility(2, 1.9, 0.85, strategies));
				
			}
		};
		

		BNEAlgorithm<Double, Double> bneAlgo = new BNEAlgorithm<>(3, context);
		bneAlgo.setCallback(callback);
		
		bneAlgo.setInitialStrategy(0, UnivariatePWLStrategy.makeTruthful(0.0, 1.0));
		bneAlgo.setInitialStrategy(2, UnivariatePWLStrategy.makeTruthful(0.0, 2.0));
		bneAlgo.makeBidderSymmetric(1, 0);
		
		bneAlgo.run();
    }
}
