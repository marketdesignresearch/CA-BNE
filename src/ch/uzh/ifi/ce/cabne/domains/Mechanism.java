package ch.uzh.ifi.ce.cabne.domains;

public interface Mechanism<Value, Bid> {
	
	public double computeUtility(int i, Value v, Bid[] bids);

}
