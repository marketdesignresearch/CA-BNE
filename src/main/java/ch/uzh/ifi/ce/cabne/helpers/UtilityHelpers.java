package ch.uzh.ifi.ce.cabne.helpers;

public final class UtilityHelpers {
	
	// Prevent instances from being created. This class should only be used via calls to static methods.
	private UtilityHelpers() {}; 

	public static double absoluteLoss(double oldu, double newu) {
		return newu - oldu;
	}
	
	public static double relativeLoss(double oldu, double newu) {
		// NOTE: One way of defining relative utility loss in the [0, \infty) range is with the formula newu / oldu - 1.
		// Semantics: increase of utility in %, e.g. triple utility is +200% ==> relative utility loss is 2
		
		// You could also define it in the [0,1) range with the formula (newu - oldu) / newu = 1 - oldu / newu
		// The semantics of that is just the absolute utility normed by newu. This is often used in numerical algorithms.
		
		// both definitions yield very similar values at small magnitudes.
		return newu / oldu - 1;
	}
	
	public static double loss(double oldu, double newu, boolean useAbsolute) {
		if (useAbsolute) {
			return UtilityHelpers.absoluteLoss(oldu, newu);
		} else {
			return UtilityHelpers.relativeLoss(oldu, newu);
		}
	}
}
