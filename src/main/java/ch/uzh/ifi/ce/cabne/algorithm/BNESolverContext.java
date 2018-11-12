package ch.uzh.ifi.ce.cabne.algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import ch.uzh.ifi.ce.cabne.BR.BRCalculator;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.domains.Mechanism;
import ch.uzh.ifi.ce.cabne.integration.Integrator;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Optimizer;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.UpdateRule;
import ch.uzh.ifi.ce.cabne.randomsampling.RandomGenerator;
import ch.uzh.ifi.ce.cabne.verification.Verifier;

public class BNESolverContext<Value, Bid> {
	public HashMap<String, String> config;
	public Mechanism<Value, Bid> mechanism;
	public Optimizer<Value, Bid> optimizer;
	public Integrator<Value, Bid> integrator;
	public List<RandomGenerator> rngs;
	public BidSampler<Value, Bid> sampler;
	public UpdateRule<Value, Bid> updateRule;
	public BRCalculator<Value, Bid> brc;
	public BRCalculator<Value, Bid> outerBRC;
	public Verifier<Value, Bid> verifier;
	
	public BRCalculator<Value, Bid> getBrc() {
		return brc;
	}
	public void setBRC(BRCalculator<Value, Bid> brc) {
		this.brc = brc;
	}
	public BRCalculator<Value, Bid> getOuterBRC() {
		return outerBRC;
	}
	public void setOuterBRC(BRCalculator<Value, Bid> outerBRC) {
		this.outerBRC = outerBRC;
	}
	public Verifier<Value, Bid> getVerifier() {
		return verifier;
	}
	public void setVerifier(Verifier<Value, Bid> verifier) {
		this.verifier = verifier;
	}
	public UpdateRule<Value, Bid> getUpdateRule() {
		return updateRule;
	}
	public void setUpdateRule(UpdateRule<Value, Bid> updateRule) {
		this.updateRule = updateRule;
	}
	public BidSampler<Value, Bid> getSampler() {
		return sampler;
	}
	public void setSampler(BidSampler<Value, Bid> sampler) {
		this.sampler = sampler;
	}
	public BNESolverContext() {
		rngs = new ArrayList<>(40);
		for (int i=0; i<40; i++) {
			rngs.add(null);
		}
	}
	public HashMap<String, String> getConfig() {
		return config;
	}
	public void setConfig(HashMap<String, String> config) {
		this.config = config;
	}
	
	public void parseConfig(String path) throws FileNotFoundException {
		File file = new File(path);
		Scanner input = new Scanner(file);
		HashMap<String, String> config = new HashMap<>();
		while (input.hasNext()) {
			String key = input.next().toLowerCase();
			String value = input.next().trim();
			config.put(key, value);
		}
		input.close();
		this.config = config;
	}
	
	public int getIntParameter(String name) {
		return Integer.parseInt(getParameter(name));
	}

	public double getDoubleParameter(String name) {
		return Double.parseDouble(getParameter(name));
	}
	
	public String getStringParameter(String name) {
		return getParameter(name);
	}
	
	public boolean getBooleanParameter(String name) {
		// Returns true if parameter "name" is present in config file and set to
		// either "true" (case insensitive) or "1".
		// Returns false if "name" is set to anything else or not present in config file.
		// Never throws an exception.
		String value = hasParameter(name) ? getParameter(name) : "false";
		return Boolean.parseBoolean(value) || (value.equals("1"));
	}
	
	public boolean hasParameter(String name) {
		return config.containsKey(name.toLowerCase());
	}
	
	private String getParameter(String name) {
		if (!config.containsKey(name.toLowerCase())) {
			throw new RuntimeException(String.format("Parameter '%s' not found in config", name));
		}
		return config.get(name.toLowerCase());
	}

	public void activateConfig(String prefix) {
		// Copies everything with a given prefix to the top level. This allows toggling between different "subconfigs".
		// e.g. with prefix="InnerLoop", InnerLoop.PatternSearch.Stepsize becomes PatternSearch.Stepsize
		prefix = prefix.toLowerCase();
		HashMap<String, String> newEntries = new HashMap<>();
		for (String key : config.keySet()) {
			if (!key.startsWith(prefix)) {
				continue;
			}
			String newkey = key.substring(prefix.length() + 1);
			newEntries.put(newkey, config.get(key));
		}
		config.putAll(newEntries);
	}
	
	public Optimizer<Value, Bid> getOptimizer() {
		return optimizer;
	}
	public void setOptimizer(Optimizer<Value, Bid> optimizer) {
		this.optimizer = optimizer;
	}

	public Mechanism<Value, Bid> getMechanism() {
		return mechanism;
	}
	public void setMechanism(Mechanism<Value, Bid> mechanism) {
		this.mechanism = mechanism;
	}
	public Integrator<Value, Bid> getIntegrator() {
		return integrator;
	}
	public void setIntegrator(Integrator<Value, Bid> integrator) {
		this.integrator = integrator;
	}
	public RandomGenerator getRng(int dimension) {

		return rngs.get(dimension);
	}
	public void setRng(int dimension, RandomGenerator rng) {
		rngs.set(dimension, rng);
	}
	
	public void advanceRngs() {
		for (int i=0; i<40; i++) {
			RandomGenerator rng = rngs.get(i);
			if (rng != null) rng.advance();
		}
	}
}
