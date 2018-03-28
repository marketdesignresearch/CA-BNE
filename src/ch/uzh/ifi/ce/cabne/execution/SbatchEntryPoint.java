package ch.uzh.ifi.ce.cabne.execution;

import java.io.IOException;

public class SbatchEntryPoint {

	public static void main(String[] args) throws InterruptedException, IOException {
		// There are 7 algorithms to choose from as well as 16 payment rules
		// The algorithms are on the outside, so it's easier to selectively run only a few of them
		// 0-15: 1-baseline
		// 16-31: 2-quasi
		// etc
		
		String entryPoint = args[0];
		int index = Integer.parseInt(args[1]);
		String configfile = args[2];
		String outputfolder = args[3];
		
		String algorithm = LLGExperimentBase.algorithms[index/16];
		index %= 16;
		
		String[] alphas = {"1.0", "2.0"};	
		String alpha = alphas[index / 8];
		index %= 8;
		
		String[] gammas = {"0.0", "0.5"};
		String gamma = gammas[index/4];
		index %= 4;
		
		String[] rules = {"quadratic", "proportional", "proxy", "nearest-bid"};
		String rule = rules[index];

		// call the class that actually performs the experiment
		String[] newargs;
		switch (entryPoint) {
		case "LLGExtrinsicBNEpass1":
			newargs = new String[] {rule, alpha, gamma, algorithm, configfile, outputfolder};
			LLGExtrinsicBNEpass1.main(newargs);
			break;
		case "LLGExtrinsicBNEpass2":
			// NOTE: this is a bit messy, we have this universal entrypoint but we expect a different number of 
			// args depending on which experiment we are running. Maybe it's possible to refactor this
			String pass1folder = args[4];
			newargs = new String[] {rule, alpha, gamma, algorithm, configfile, outputfolder, pass1folder};
			LLGExtrinsicBNEpass2.main(newargs);
			break;
		}
		
	}
}
