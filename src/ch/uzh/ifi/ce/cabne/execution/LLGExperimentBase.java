package ch.uzh.ifi.ce.cabne.execution;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;

import ch.uzh.ifi.ce.cabne.BR.AdaptivePWLBRCalculator;
import ch.uzh.ifi.ce.cabne.BR.PWLBRCalculator;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.integration.MCIntegrator;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Optimizer;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Pattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.PatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.TestingPatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.UnivariateBrentSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.UnivariatePattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.UnivariateDampenedUpdateRule;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.UnivariateLinearUpdateRule;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.UpdateRule;
import ch.uzh.ifi.ce.cabne.randomsampling.CommonRandomGenerator;
import ch.uzh.ifi.ce.cabne.randomsampling.NaiveRandomGenerator;
import ch.uzh.ifi.ce.cabne.randomsampling.QuasiRandomGenerator;

public abstract class LLGExperimentBase {
	public static String[] algorithms = {"1baseline", "2quasi", "3common", "4dampened", "5pattern", "6ttest", "7adaptive"};
	
	public static void configureAlgorithm(String algorithmName, BNESolverContext<Double, Double> context) {
		double targetepsilon = Double.parseDouble(context.config.get("epsilon"));
		
		// note that this is not used when using statistical tests
		context.setIntegrator(new MCIntegrator<Double, Double>(context));
		
		// create optimizers
		Pattern<Double> pattern = new UnivariatePattern();
		Optimizer<Double, Double> brentSearch = new UnivariateBrentSearch(context);
		Optimizer<Double, Double> patternSearch = new PatternSearch<>(context, pattern);
		Optimizer<Double, Double> testingPatternSearch = new TestingPatternSearch<>(context, pattern);
		
		// create update rules
		UpdateRule<Double, Double> linearUpdate = new UnivariateLinearUpdateRule(0.5);
		UpdateRule<Double, Double> dampenedUpdate = new UnivariateDampenedUpdateRule(0.2, 0.7, 0.5 / targetepsilon, true);
		
		switch (algorithmName) {
		case "1baseline":
			context.setRng(2, new NaiveRandomGenerator(2));
			context.setRng(1, new NaiveRandomGenerator(1)); // need this to draw correlated samples
			context.activateConfig("naive"); // set MCsamples to a number appropriate for naive
			context.setOptimizer(brentSearch);
			context.setUpdateRule(linearUpdate);
			context.setBRC(new PWLBRCalculator(context));
			break;
		case "2quasi":
			context.setRng(2, new QuasiRandomGenerator(2));
			context.setRng(1, new QuasiRandomGenerator(1));
			context.activateConfig("quasi");
			context.setOptimizer(brentSearch);
			context.setUpdateRule(linearUpdate);
			context.setBRC(new PWLBRCalculator(context));
			break;
		case "3common":
			context.setRng(2, new CommonRandomGenerator(2));
			context.setRng(1, new CommonRandomGenerator(1));
			context.activateConfig("common");
			context.setOptimizer(brentSearch);
			context.setUpdateRule(linearUpdate);
			context.setBRC(new PWLBRCalculator(context));
			break;
		case "4dampened":
			context.setRng(2, new CommonRandomGenerator(2));
			context.setRng(1, new CommonRandomGenerator(1));
			context.activateConfig("common");
			context.setOptimizer(brentSearch);
			context.setUpdateRule(dampenedUpdate);
			context.setBRC(new PWLBRCalculator(context));
			break;
		case "5pattern":
			context.setRng(2, new CommonRandomGenerator(2));
			context.setRng(1, new CommonRandomGenerator(1));
			context.activateConfig("common");
			context.setOptimizer(patternSearch);
			context.setUpdateRule(dampenedUpdate);
			context.setBRC(new PWLBRCalculator(context));
			break;
		case "6ttest":
			context.setRng(2, new CommonRandomGenerator(2));
			context.setRng(1, new CommonRandomGenerator(1));
			context.activateConfig("common");
			context.setOptimizer(testingPatternSearch);
			context.setUpdateRule(dampenedUpdate);
			context.setBRC(new PWLBRCalculator(context));
			break;
		case "7adaptive":
			context.setRng(2, new CommonRandomGenerator(2));
			context.setRng(1, new CommonRandomGenerator(1));
			context.activateConfig("common");
			context.setOptimizer(testingPatternSearch);
			context.setUpdateRule(dampenedUpdate);
			context.setBRC(new AdaptivePWLBRCalculator(context));
			break;
		default:
			throw new RuntimeException("Unknown algorithm");
		}
	}
	
	public static double analyticalBNE(double value, String paymentrule, double alpha, double gamma) {
    	// This method assumes that parameters alpha and gamma are within their allowed ranges (0,\infty) resp [0,1]
		
		switch (paymentrule) {
		case "proportional":
		case "quadratic":
	    	if (gamma == 1.0) {
	    		return value * (2.0/3.0);
	    	} else if (alpha == 1.0) {
	    		double k = 2 / (2+gamma);
	    		double d = (3*k*k - 2*k*Math.sqrt(3*k-1)) / (3*k-2);
	    		return Math.max(0, k*value - d);
	    	} else {
	    		BrentSolver solver = new BrentSolver(1e-9, 1e-9);
	    		final double k = 2.0 / (2.0+gamma);
	    		
	            UnivariateFunction f = d -> {
	            	return Math.pow(d, alpha + 1.0)/((1.0+alpha)*Math.pow(k, alpha)) - (3.0*d*k)/(3.0*k-2.0) + (alpha*k)/(alpha+1.0);
	            };
	    		double d = solver.solve(2000, f, 0.0, k);
	    		return Math.max(0, k*value - d);
	    	}
		case "proxy":
			if (gamma == 1.0) {
	    		return value;
	    	} else if (alpha == 1.0) {
	    		return Math.max(0, 1 + Math.log(gamma + (1-gamma)*value) / (1 - gamma));
	    	} else if (gamma == 0.0) { 
	    		return Math.max(0, (Math.pow(value, 1-alpha)-alpha) / (1-alpha));
	    	} else if (alpha == 2.0) {
	    		double C = 1 - (1 / Math.sqrt(gamma * (1-gamma)))   *   Math.atan(Math.sqrt((1-gamma)/gamma));
	    		return Math.max(0, (1 / Math.sqrt(gamma * (1-gamma)))   *   Math.atan(Math.sqrt((1-gamma)/gamma)*value) + C);
	    	} else {
	    		throw new RuntimeException(String.format("analytical solution for alpha=%f, gamma=%f not implemented yet", alpha, gamma));
	    	}
		case "nearest-bid":
			if (gamma == 1.0) {
	    		return value/2.0;
	    	} else if (alpha == 1.0) {
	    		return (1/(1-gamma)) * (Math.log(2) - Math.log(2 - (1-gamma) * value));
	    	} else if (alpha == 2.0) {
	    		double tmp1 = Math.log(Math.sqrt(2/(1-gamma)) + value);
	    		double tmp2 = Math.log(Math.sqrt(2/(1-gamma)) - value);
	    		return (1/Math.sqrt(8-8*gamma)) * (tmp1 - tmp2);
	    	} else {
	    		throw new RuntimeException(String.format("analytical solution for alpha=%f, gamma=%f not implemented yet", alpha, gamma));
	    	}
		default:
			throw new RuntimeException("Unknown rule");
		}
    }
}
