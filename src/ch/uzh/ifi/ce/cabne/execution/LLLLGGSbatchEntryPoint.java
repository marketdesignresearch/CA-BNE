package ch.uzh.ifi.ce.cabne.execution;

import java.io.IOException;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;

public class LLLLGGSbatchEntryPoint {

	public static void main(String[] args) throws InterruptedException, IOException {
		
		// TODO: read in args
		String configfile = args[0];
		String strategyFile = args[1];
		String outputFolder = args[2];
		// (index is array task (0-999) + offset (in increments of 1000)) * batchsize
		int batchsize = Integer.parseInt(args[5]);
		int minIndex = (Integer.parseInt(args[3]) + Integer.parseInt(args[4])) * batchsize;
		String state = args[6]; 
		
		// it's not so clean to create a context just to read one value, but we want it to be consistent
		BNESolverContext<Double[], Double[]> context = new BNESolverContext<>();
		context.parseConfig(configfile);
		context.activateConfig(state);
		int gridsize = context.getIntParameter("gridsize");

		int maxIndex = Math.min(minIndex + batchsize, gridsize*gridsize + gridsize*(gridsize+1)/2);

		System.out.format("minIndex: %d, maxIndex: %d\n", minIndex, maxIndex);

		
		// finally, we have all parameters needed to call the entry point doing the actual work
		LLLLGGBestResponseBatch.main(minIndex, maxIndex, configfile, strategyFile, outputFolder, state);
	}
}
