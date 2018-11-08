package ch.uzh.ifi.ce.cabne.domains.LLLLGG;

import java.util.HashMap;
import java.util.Map;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;


public class LLLLGGParametrizedCoreSelectingRule extends LLLLGGMechanism {
	SolverClient solverClient = new SolverClient();
	
	final double amplification;
	final String refPointName;
	final String weightName;

	public LLLLGGParametrizedCoreSelectingRule(String refPointName, String weightName, double amplification) {
		this.amplification = amplification;
		this.refPointName = refPointName.toLowerCase();
		this.weightName = weightName.toLowerCase();
	}
	
	private void createWeightedReferenceObjective(MIP mip, Variable[] variables, double[] vcg, double[] IR, double minrev) {
		// compute reference point.
		double[] referencePoint;
		switch (refPointName) {
		case "zero":
			referencePoint = new double[6];
			break;
		case "vcg":
			referencePoint = vcg;
			break;
		case "mirroredvcgpayoff":
	    	referencePoint = new double[6];
	    	double vcgPayoffRevenue = 0.0;
	    	for (int i=0; i<6; i++) {
	    		referencePoint[i] = IR[i] - vcg[i];
	    		vcgPayoffRevenue += referencePoint[i];
	    	}
			double adjustment = vcgPayoffRevenue - minrev; 
	    	if (adjustment > 0.0) {
	    		for (int i=0; i<6; i++) {
	        		referencePoint[i] -= 2.0 / 6.0 * adjustment;
	        	}
	    	}
			break;
		default:
			throw new RuntimeException("don't recognize reference point");
		}
		
		// compute weights.
		double[] inverseWeights;
		switch (weightName) {
		case "vcgpayoff":
			inverseWeights = new double[6];
			for (int i=0; i<6; i++) {
				inverseWeights[i] = 1.0 / Math.max(IR[i] - vcg[i], 1e-5); 
			}
			break;
		case "bids":
			inverseWeights = new double[6];
			for (int i=0; i<6; i++) {
				inverseWeights[i] = 1.0 / Math.max(IR[i], 1e-5); 
			}
			break;
		case "inversevcgpayoff":
			inverseWeights = new double[6];
			for (int i=0; i<6; i++) {
				inverseWeights[i] = IR[i] - vcg[i]; 
			}
			break;
		case "inversebids":
			inverseWeights = IR;
			break;
		default:
			throw new RuntimeException("don't recognize weight");
		}
		
		// Finally, set objective function
		mip.setObjectiveMax(false);
		for (int i=0; i<6; i++) {
			// add an objective term for 1/(w_i)^a * (p_i - ref_i)^2  ~=  1/(w_i)^a * p_i^2 - 2/(w_i)^a * p_i * ref_i
			double amplifiedWeight = Math.pow(inverseWeights[i], amplification);
			amplifiedWeight = Math.min(amplifiedWeight, 0.2499 * MIP.MAX_VALUE);
			mip.addObjectiveTerm(-2.0 * amplifiedWeight * referencePoint[i], variables[i]);
			mip.addObjectiveTerm(amplifiedWeight, variables[i], variables[i]);
		}
	}

	@Override
	public double computeUtility(int i, Double[] v, Double[][] bids, int[] alloc) {
		if ((encodeWinners(alloc)>>i)%2 == 0) {
			// if player i doesn't win anything, there is no need to compute the core payments
			return 0.0;
		}
		double[] vcgPayments = wd.computeVCG(bids, alloc);
		double[] corePayments = projectToCore(bids, alloc, vcgPayments);
		double utility = -corePayments[i];
		for (int bundle : alloc) {
			if (bundle/2 == i) utility += v[bundle%2];
		}
		return utility;
	}
	
	public double[] projectToCore(Double[][] bids, int[] alloc, double[] vcgPoint) {
		MIP mip = new MIP();
		mip.setSolveParam(SolveParam.DISPLAY_OUTPUT, false); // make it not output megabytes of debugging info
		mip.setSolveParam(SolveParam.THREADS, 1);
		
		mip.setSolveParam(SolveParam.CALC_DUALS, Boolean.TRUE);
		mip.setSolveParam(SolveParam.THREADS, 1);
		mip.setSolveParam(SolveParam.LP_OPTIMIZATION_ALG, 1);
		mip.setSolveParam(SolveParam.DISPLAY_OUTPUT, false);
		mip.setSolveParam(SolveParam.MARKOWITZ_TOLERANCE, .2);
		mip.setSolveParam(SolveParam.ABSOLUTE_VAR_BOUND_GAP, 1e-6);
		mip.setSolveParam(SolveParam.CONSTRAINT_BACKOFF_LIMIT, .0d);
		mip.setSolveParam(SolveParam.ABSOLUTE_OBJ_GAP, 1e-6);
		mip.setSolveParam(SolveParam.RELATIVE_OBJ_GAP, 1e-6);
		mip.setSolveParam(SolveParam.OBJ_TOLERANCE, 1e-6);
		mip.setSolveParam(SolveParam.PROBLEM_FILE, "");
		
		Variable[] variables = new Variable[6];
		double[] upperBounds = new double[6];
		for (int bundleIndex : alloc) {
			int i = bundleIndex/2;
			// individual rationality constraint. If not set by this loop, it is 0.0 (java array initialization).
			upperBounds[i] = bids[i][bundleIndex%2];
		}
		for (int i=0; i<6; i++) {
			variables[i] = new Variable("p_" + i, VarType.DOUBLE, 0.0, upperBounds[i]);
			mip.add(variables[i]);
		}
		
		// Instead of iterating over all coalitions and for each coalition finding the best allocation for that coalition,
		// we just iterate over all feasible allocations (which are precomputed) and add one constraint for each.
		// Generating constraints via feasible allocations is better because we don't have to run any WD,
		// the allocation and constraint directly correspond to each other.
		// To avoid putting dominated constraints into the QP, accumulate lower bounds into a HashMap, then put only the best
		// from each coalition into the QP.		
		
		// Note that no matter which way we do it, we will get some dominated constraints. If we have coalitions
		// L' \subseteq L, then the constraints generated by L' are weaker most of the time, but not always (i.e. when 
		// some bidder in L \ L' wins a better bundle in the efficient allocation than within the coalition L).
		
		int winners = encodeWinners(alloc);
		Map<Integer, Double> boundPerCoalition = new HashMap<>(); 
		for (int[] alternativeAlloc : LLLLGGWD.subsolutions) {
			// build constraint as per formulation is (11.22) in Sven's book: 
			// non-coalition payment >= coalitional value (ideal allocation) - coalitional value (actual allocation)
			int coalition = encodeWinners(alternativeAlloc);
			if ((coalition & winners) == coalition) {
				// coalition is a superset of the winners
				// we always have valueDiff < 0, otherwise alternativeAlloc would be more efficient than alloc.
				continue;
			}
			
			// Compute additional utility this coalition could get under the alternative allocation.
			// by subtracting existing utility from potential utility.
			double valueDiff = 0.0;
			for (int bundleIndex : alternativeAlloc) {
				valueDiff += bids[bundleIndex/2][bundleIndex%2];
			}
			for (Integer bundleIndex : alloc) {
				int i = bundleIndex/2;
				if ((coalition>>i)%2 == 1) {
					valueDiff -= bids[i][bundleIndex%2];
				}
			}
			if (valueDiff <= 0.0) {
				continue;
			}
			
			double previousValueDiff = boundPerCoalition.getOrDefault(coalition, 0.0);
			if (valueDiff > previousValueDiff) {
				boundPerCoalition.put(coalition, valueDiff);
			}
		}
		
		for (Map.Entry<Integer, Double> e : boundPerCoalition.entrySet()) {
			int coalition = e.getKey();
			Constraint c = new Constraint(CompareType.GEQ, e.getValue());
			for (int i=0; i<6; i++) {
				c.addTerm(1 - (coalition>>i)%2, variables[i]);
			}
			mip.add(c);
		}
		
		// First, need to find the MRC.
		mip.setObjectiveMax(false);
		for (int i=0; i<6; i++) {
			mip.addObjectiveTerm(1.0, variables[i]);
		}
    	IMIPResult result = solverClient.solve(mip);
    	
    	// Secondly, add the MRC constraint to the MIP
    	Constraint c = new Constraint(CompareType.LEQ, result.getObjectiveValue() + 1e-5);
    	for (int i=0; i<6; i++) {
    		c.addTerm(1.0, variables[i]);
    	}
    	mip.add(c);
    	
		// Finally, define the objective function and solve the QP	
    	mip.clearProposedValues();
		mip.clearObjective();
		createWeightedReferenceObjective(mip, variables, vcgPoint, upperBounds, result.getObjectiveValue());
		
    	result = solverClient.solve(mip);
    	double[] payments = new double[6];
    	for (int i=0; i<6; i++) {
    		payments[i] = result.getValue(variables[i]);
    	}
		return payments;
	}

}
