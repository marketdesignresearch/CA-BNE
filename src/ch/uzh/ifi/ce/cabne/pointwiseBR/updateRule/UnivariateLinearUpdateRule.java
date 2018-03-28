package ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule;

public class UnivariateLinearUpdateRule implements UpdateRule<Double, Double> {
	double weight;
	
	public UnivariateLinearUpdateRule(double weight) {
		this.weight = weight;
	}

	@Override
	public Double update(Double v, Double oldbid, Double newbid, double oldutility, double newutility) {
		return oldbid * (1-weight) + newbid * weight;
	}
}
