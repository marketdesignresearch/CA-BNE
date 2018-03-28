package ch.uzh.ifi.ce.cabne.strategy;

public interface Strategy<Value, Bid> {
	
	Bid getBid(Value v);
	
	Value getMaxValue();
}
