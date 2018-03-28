package ch.uzh.ifi.ce.cabne.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.SortedMap;

import ch.uzh.ifi.ce.cabne.BR.BRCalculator.Result;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.Mechanism;
import ch.uzh.ifi.ce.cabne.domains.LLG.AusubelBaranovLLGSampler;
import ch.uzh.ifi.ce.cabne.domains.LLG.NearestBid;
import ch.uzh.ifi.ce.cabne.domains.LLG.Proportional;
import ch.uzh.ifi.ce.cabne.domains.LLG.Proxy;
import ch.uzh.ifi.ce.cabne.domains.LLG.Quadratic;
import ch.uzh.ifi.ce.cabne.integration.MCIntegrator;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.strategy.UnivariatePWLStrategy;


public class LLGIntrinsicBNEpass1 extends LLGExperimentBase {
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		// Extract arguments.
		// this entry point is called with the name of the rule, alpha, gamma, etc.
		// It is meant to be called directly. Another entrypoint called SbatchEntryPoint can be called with
		// a single int as argument.
		String mechanismName = args[0];
		double alpha = Double.parseDouble(args[1]);
		double gamma = Double.parseDouble(args[2]);
		String algorithmName = args[3];
		String configfile = args[4];
		Path outputfolder = Paths.get(args[5]);
		
		Path outputFile = outputfolder.resolve(String.format("%s-%2.1f-%2.1f-%s", mechanismName, alpha, gamma, algorithmName));

		Mechanism<Double, Double> mechanism;
		switch (mechanismName) {
			case "proportional":
				mechanism = new Proportional();
				break;
			case "quadratic":
				mechanism = new Quadratic();
				break;
			case "proxy":
				mechanism = new Proxy();
				break;
			case "nearest-bid":
				mechanism = new NearestBid();
				break;
			default:
				throw new RuntimeException("Unknown rule");
		}
		
		// create context
		BNESolverContext<Double, Double> context = new BNESolverContext<>();
		context.parseConfig(configfile);
		context.setMechanism(mechanism);
		context.setSampler(new AusubelBaranovLLGSampler(alpha, gamma, context));
		context.setIntegrator(new MCIntegrator<Double, Double>(context)); // note that this is not used when using statistical tests
		
		// read general algorithm settings from config
		int maxIters = Integer.parseInt(context.config.get("maxiters"));
		double targetEpsilon = Double.parseDouble(context.config.get("epsilon"));
		
		configureAlgorithm("7adaptive", context); // TODO: need to do this whenever we switch from inner to outer loop
		
		// create starting strategies
		ArrayList<Strategy<Double, Double>> strats = new ArrayList<>(3);
		strats.add(0, UnivariatePWLStrategy.makeTruthful(0.0, 1.0));
		strats.add(1, UnivariatePWLStrategy.makeTruthful(0.0, 1.0));
		strats.add(2, UnivariatePWLStrategy.makeTruthful(0.0, 2.0));
		
		// -----------------------------------------------------------------------------------
		// finished setup, now comes the main loop
		// -----------------------------------------------------------------------------------
		

		long startTime = System.currentTimeMillis();
		Boolean converged = false;
		int iteration = 1;
		
		// String builder that assembles the output.
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("0 %d 0.0 0.0 1.0 1.0\n", startTime));
		
		for (; iteration <= maxIters; iteration++) {
			// This is the outer loop. First thing we do is go into the inner loop
			
			for (;iteration <= maxIters; iteration++) {
				// This is the inner loop
				System.out.println("Starting Inner Iteration " + iteration);
				context.advanceRngs();
				context.activateConfig("innerloop");
				context.activateConfig("common");
				
				ArrayList<Strategy<Double, Double>> newstrats = new ArrayList<>(3);
				Result<Double, Double> result = context.brc.computeBR(0, strats);
				UnivariatePWLStrategy s = (UnivariatePWLStrategy) result.br;
				newstrats.add(0, s);
				newstrats.add(1, s);
				newstrats.add(2, strats.get(2));
				
				// print out strategy
				builder.append(String.format("%d %d", iteration, System.currentTimeMillis()));
				SortedMap<Double, Double> data = s.getData();
				for (double key : data.keySet()) {
					double value = data.get(key);
					builder.append(String.format(" %f %f", key, value));
				}
				builder.append("\n");
				
				// update strategy
				strats = newstrats;
				
				// if we are converged according to the eps on the control points where we computed best responses, then
				// break out to the outer loop
				if (result.epsilonAbs <= targetEpsilon) {
					break;
				}
			}
			
			iteration++;
			System.out.println("Starting Outer Iteration " + iteration);
			context.advanceRngs();
			context.activateConfig("outerloop");
			context.activateConfig("common");
			
			ArrayList<Strategy<Double, Double>> newstrats = new ArrayList<>(3);
			Result<Double, Double> result = context.brc.computeBR(0, strats);
			UnivariatePWLStrategy s = (UnivariatePWLStrategy) result.br;
			newstrats.add(0, s);
			newstrats.add(1, s);
			newstrats.add(2, strats.get(2));
			
			// print out strategy
			builder.append(String.format("%d %d", iteration, System.currentTimeMillis()));
			SortedMap<Double, Double> data = s.getData();
			for (double key : data.keySet()) {
				double value = data.get(key);
				builder.append(String.format(" %f %f", key, value));
			}
			builder.append("\n");
			
			// update strategy
			strats = newstrats;
			
			if (result.epsilonAbs <= targetEpsilon) {
				converged = true;
				break;
			}
		}
			
		long endTime = System.currentTimeMillis();
		
		// write out analytical BNE
		builder.append("-1 -1");
		for (double v=0.0; v<=1.01; v += 0.01) {
			builder.append(String.format(" %f %f", v, analyticalBNE(v, mechanismName, alpha, gamma)));
		}

		Files.write(
			outputFile, builder.toString().getBytes(), 
			StandardOpenOption.CREATE, 
			StandardOpenOption.WRITE, 
			StandardOpenOption.TRUNCATE_EXISTING
		);
		
		
		// Verification Step. We want to compute 3 things here:
		//   1) verification eps
		//	 2) distance from analytical BNE
		//	 3) analytically proven eps if applicable (maybe put this into its own pass2
		

		// New stringbuilder to write out the verification result.
		builder = new StringBuilder("Iter    absolute eps  time(ms)  total time(ms)\n");
		//builder.append(String.format("0 %d 0.0 0.0 1.0 1.0\n", System.currentTimeMillis()));
		
		
		System.out.println("Starting Verification Step");
		context.advanceRngs();
		context.activateConfig("verificationstep");
		context.activateConfig("common");
		
		// TODO: change strategy to be PWC?
		// change brc to be a verifier
		//context.setBRC(new PointwiseVerifier(context));
		
		Result<Double, Double> result = context.brc.computeBR(0, strats);
		long cumulativeComputationTime = endTime - startTime;
		
		builder.append(String.format(" %3d   %12.9f     %6d          %6d\n", 
				 iteration, 
				 result.epsilonAbs, 
				 cumulativeComputationTime,
				 cumulativeComputationTime));
	}
}
