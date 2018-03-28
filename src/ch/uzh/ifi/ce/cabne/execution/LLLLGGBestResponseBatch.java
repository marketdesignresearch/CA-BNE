package ch.uzh.ifi.ce.cabne.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.ce.cabne.Helpers;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.Mechanism;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGParametrizedCoreSelectingRule;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGPayAsBid;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGProportional;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGProxy;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGQuadratic;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGSampler;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.LLLLGGStrategyParser;
import ch.uzh.ifi.ce.cabne.domains.LLLLGG.CCG.LLLLGGQuadraticCCG;
import ch.uzh.ifi.ce.cabne.integration.MCIntegrator;
import ch.uzh.ifi.ce.cabne.pointwiseBR.MultivariateCrossPattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Optimizer;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Pattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.PatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.TestingPatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.MultivariateDampenedUpdateRule;
import ch.uzh.ifi.ce.cabne.randomsampling.CommonRandomGenerator;
import ch.uzh.ifi.ce.cabne.strategy.ConstantGridStrategy2D;
import ch.uzh.ifi.ce.cabne.strategy.GridStrategy2D;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;


public class LLLLGGBestResponseBatch {
	
	public static void main(int minIndex, int maxIndex, String configfile, String strategyFile, String outputFolder, String state) throws InterruptedException, IOException {		
		Path inputFile = Paths.get(strategyFile); // TODO: better names for these variables
		
		long starttime = System.currentTimeMillis();
		
		// create context and read config
		BNESolverContext<Double[], Double[]> context = new BNESolverContext<>();
		context.parseConfig(configfile);
		context.activateConfig(state);
		
		double targetepsilon = context.getDoubleParameter("epsilon");	
		int gridsize = context.getIntParameter("gridsize");
		
		// create mechanism
		Mechanism<Double[], Double[]> mechanism;
		if (context.getBooleanParameter("hacks.useccg")) {
			mechanism = new LLLLGGQuadratic();	
		} else {
			mechanism = new LLLLGGQuadraticCCG();	
		}
		
		if (context.hasParameter("hacks.paymentrule")) {
			switch (context.getStringParameter("hacks.paymentrule")) {
			case "firstprice":
				mechanism = new LLLLGGPayAsBid();
				break;
			case "proportional":
				mechanism = new LLLLGGProportional();
				break;
			case "proxy":
				mechanism = new LLLLGGProxy();
				break;
			case "allroundergeneric":
				// HACK to help out Benedikt
				String ref = context.getStringParameter("hacks.paymentrule.ref");
				String weight = context.getStringParameter("hacks.paymentrule.weight");
				double amp = context.getDoubleParameter("hacks.paymentrule.amp");
				mechanism = new LLLLGGParametrizedCoreSelectingRule(ref, weight, amp);
			case "":
			case "quadratic":
				// already set payment rule (same as if this config was not present
				break;
			default:
				throw new RuntimeException("don't recognize payment rule");
			}
		}
		
		// create pattern
		Pattern<Double[]> pattern = new MultivariateCrossPattern(2);
		//Pattern<Double[]> pattern = new MultivariateGaussianPattern(2, patternSize);
		
		// compute rng offset (we use common random numbers, and we don't want them to be the same each iteration)
		// NOTE: basename is not provided by Path class. Workaround: Convert it into a File, where basename is implemented implicitly
		String basename = inputFile.toFile().getName(); // e.g. "iter1.strats", "iter97.strats"
		String iteration = basename.substring(4, basename.length()-7);
		int rngOffset = Integer.parseInt(iteration) * context.getIntParameter("rngoffsetperiter");
		System.out.format("iteration %s, rng offset %d\n", iteration, rngOffset);
		
		// initialize all algorithm pieces
		context.setOptimizer(new PatternSearch<Double[], Double[]>(context, pattern));
		context.setIntegrator(new MCIntegrator<Double[], Double[]>(context));
		context.setMechanism(mechanism);
		context.setRng(10, new CommonRandomGenerator(10, rngOffset));
		context.setSampler(new LLLLGGSampler(context));
		context.setUpdateRule(new MultivariateDampenedUpdateRule<Double[]>(0.2, 0.7, 0.5 / targetepsilon, true));
		
		// HACK: optionally add t-tests
		if (context.getBooleanParameter("hacks.usetesting")) {
			context.setOptimizer(new TestingPatternSearch<Double[], Double[]>(context, pattern));
		}
		
		// --------------------
		// NEW SECTION
		// --------------------
		
		List<Strategy<Double[], Double[]>> strats = null;
		
		for (int batch=minIndex; batch<maxIndex; batch++) {
			int index = batch;
		
			// There are n*n*2 input items
			// half for the global player, half for the local one
			
			// The first n*n array jobs are for the local players, 
			// the next n(n+1)/2 for the global players (fewer because of symmetry)
			int isGlobal = index / (gridsize*gridsize);
			index %= (gridsize*gridsize);
			
			// player is 0 or 4, depending if local or global
			int i = 4*isGlobal;
			
			// gridX, gridY are integers in {0, ..., gridsize}
			// x,y are numbers in [0,1] for local, [0,2] for global (including boundaries)
			double gridX, gridY;
			if (isGlobal == 0) {
				gridX = index / gridsize;
				gridY = index % gridsize;
			} else {
				// generate gridpoints where gridX >= gridY
				// e.g. gridsize=3 --> (0,0), (1,0), (1,1), (2,0), (2,1), (2,2)
				gridX = 0;
				gridY = index;
				for (int x=1; x<=gridY; x++) {
					gridY -= x;
					gridX++;
				}
			}
			double x = gridX / (gridsize-1) * (1 + isGlobal);
			double y = gridY / (gridsize-1) * (1 + isGlobal);
			Double[] v = new Double[] { x, y}; // TODO: make nicer
			
			Path outputFile = Paths.get(outputFolder, String.format("%04d-%s.tmp", index, isGlobal==0 ? "L" : "G"));
			if (Files.exists(outputFile)) {
				continue;
			}
			
			// read starting strategies from file if not read yet
			LLLLGGStrategyParser parser = new LLLLGGStrategyParser();
			strats = new ArrayList<>();
			if (state.equalsIgnoreCase("verificationstep")) {
				// need to convert the parsed strategies from PWL to PWC
				for (GridStrategy2D s : parser.parse(inputFile)) {
					strats.add(new ConstantGridStrategy2D(s));
				}
			} else {
				for (GridStrategy2D s : parser.parse(inputFile)) {
					strats.add(s);
				}
			}
			
			// compute BR for either local or global player
			Double[] oldbid = strats.get(i).getBid(v);
			Optimizer.Result<Double[]> result = context.optimizer.findBR(i, v, oldbid, strats);
			double epsilonAbs = Helpers.absoluteUtilityLoss(result.oldutility, result.utility);
			//double epsilonRel = Helpers.relativeUtilityLoss(result.oldutility, result.utility);
			Double[] newbid = context.updateRule.update(v, oldbid, result.bid, result.oldutility, result.utility);
			
			// write out bid, epsilon, runtime, etc
			long endtime = System.currentTimeMillis();
	        StringBuilder builder = new StringBuilder("{\n"); 
	        builder.append(String.format("  'player': %d,\n", i));
	        builder.append(String.format("  'value': (%7.6f, %7.6f),\n", v[0], v[1]));
	        builder.append(String.format("  'bid': (%7.6f, %7.6f),\n", newbid[0], newbid[1]));
	        builder.append(String.format("  'br_utility': %7.6f,\n", result.utility));
	        builder.append(String.format("  'utility_loss': %7.6f,\n", epsilonAbs));
	        builder.append(String.format("  'runtime_ms': %d,\n", endtime - starttime));
	        builder.append("}");
			Files.write(outputFile, builder.toString().getBytes(), 
					    StandardOpenOption.CREATE, 
					    StandardOpenOption.WRITE, 
					    StandardOpenOption.TRUNCATE_EXISTING);
			starttime = endtime;
		}
    }	
}
