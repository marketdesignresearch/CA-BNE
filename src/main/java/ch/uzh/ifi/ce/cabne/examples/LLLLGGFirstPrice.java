package ch.uzh.ifi.ce.cabne.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import ch.uzh.ifi.ce.cabne.BR.Grid2DBRCalculator;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithm;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithmCallback;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGPayAsBid;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGSampler;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGStrategyWriter;
import ch.uzh.ifi.ce.cabne.integration.MCIntegrator;
import ch.uzh.ifi.ce.cabne.pointwiseBR.MultivariateCrossPattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.PatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.MultivariateDampenedUpdateRule;
import ch.uzh.ifi.ce.cabne.randomsampling.CommonRandomGenerator;
import ch.uzh.ifi.ce.cabne.strategy.GridStrategy2D;
import ch.uzh.ifi.ce.cabne.verification.BoundingVerifier2D;


public class LLLLGGFirstPrice {
	
	public static void main(String[] args) throws InterruptedException, IOException {		

		// create context and read config
		BNESolverContext<Double[], Double[]> context = new BNESolverContext<>();
		String configfile = args[0];
		context.parseConfig(configfile);
		
		double targetepsilon = context.getDoubleParameter("epsilon");

		// initialize all algorithm pieces
		context.setOptimizer(new PatternSearch<>(context, new MultivariateCrossPattern(2)));
		context.setIntegrator(new MCIntegrator<>(context));
		context.setRng(10, new CommonRandomGenerator(10));
		context.setUpdateRule(new MultivariateDampenedUpdateRule<>(0.2, 0.7, 0.5 / targetepsilon, true));
		context.setBRC(new Grid2DBRCalculator(context));
		context.setOuterBRC(new Grid2DBRCalculator(context));
		context.setVerifier(new BoundingVerifier2D(context));
		
		// instanciate auction setting
		context.setMechanism(new LLLLGGPayAsBid());
		context.setSampler(new LLLLGGSampler(context));
		
		BNEAlgorithm<Double[], Double[]> bneAlgo = new BNEAlgorithm<>(6, context);
		LLLLGGStrategyWriter writer = new LLLLGGStrategyWriter();
		
		// add bidders, giving each an initial strategy and telling the algorithm which ones to update.
		// bidder 0 (first local bidder) does a best response in each iteration
		// bidder 1 (second local bidder) plays symmetrically to bidder 0
		// bidder 2 (global bidder) plays truthful and thus doesn't update his strategy.
		bneAlgo.setInitialStrategy(0, GridStrategy2D.makeTruthful(1.0, 1.0));
		bneAlgo.setInitialStrategy(4, GridStrategy2D.makeTruthful(2.0, 2.0));
		bneAlgo.makeBidderSymmetric(1, 0);
		bneAlgo.makeBidderSymmetric(2, 0);
		bneAlgo.makeBidderSymmetric(3, 0);
		bneAlgo.makeBidderSymmetric(5, 4);
		
		// create callback that reports epsilon after each iteration
		BNEAlgorithmCallback<Double[], Double[]> callback = (iteration, type, strategies, epsilon) -> {	
			System.out.format("iteration: %d, type %s, epsilon %7.6f\n", iteration, type, epsilon);
			String s = writer.write((GridStrategy2D) strategies.get(0), (GridStrategy2D) strategies.get(4), iteration);
			
			Path outputFile = Paths.get(args[1]).resolve(String.format("iter%03d.strats", iteration));

			try {
				Files.write(
					outputFile, s.getBytes(), 
					StandardOpenOption.CREATE, 
					StandardOpenOption.WRITE, 
					StandardOpenOption.TRUNCATE_EXISTING
				);
			} catch (IOException e) {
			}
		};
		bneAlgo.setCallback(callback);
		
		BNEAlgorithm.Result<Double[], Double[]> result;
		result = bneAlgo.run();
		System.out.format("Algorithm finished with eps=%f", result.epsilon);
    }
}
