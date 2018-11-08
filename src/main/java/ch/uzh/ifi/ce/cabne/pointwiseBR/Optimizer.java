package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

// this is the class in charge of optimizing the expected utility


public abstract class Optimizer<Value, Bid> {
	public static class Result<Bid> {
		public Bid bid;
		public double utility;
		public double oldutility;
		
		public Result(Bid bid, double utility, double oldutility) {
			this.bid = bid;
			this.utility = utility;
			this.oldutility = oldutility;
		}
	}
	
	BNESolverContext<Value, Bid> context;
	
	public Optimizer(BNESolverContext<Value, Bid> context) {
		this.context = context;
	}

	public abstract Result<Bid> findBR(int i, Value v, Bid oldbid, List<Strategy<Value, Bid>> s);
}
