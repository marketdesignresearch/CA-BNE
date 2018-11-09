package ch.uzh.ifi.ce.cabne.strategy;

public class QuadraticGridHelper {
	double vMax;
	int N;
	double[] coefficients;  // coefficients of polynomials (quadratic and linear, constant is always 0)

	public QuadraticGridHelper(int n, double vMax, double factor) {
		super();
		this.vMax = vMax;
		N = n;

		// compute polynomial coefficients
		coefficients = initPolynomial(factor);
	}

	private double[] initPolynomial(double factor) {
	    if (factor <= 0.0 || factor >= 1.0) {
	    	// In theory, factor == 1.0 should work. however, this is numerically unstable, 
	    	// even when computing a = 0 and b = N - 1 exactly.
	        throw new RuntimeException("Factor must be in (0,1) interval.");
	    }
	    
	    // p(x) = ax^2 + b*x + c
	    // we have that 
	    //    p(0) 						= 0
	    //    p(vmax) 					= N-1
	    //    p(vmax*(1-factor/(N-1))) 	= N-2
	    // The last condition means that the 2nd highest gridpoint is closer together than a regular grid (by factor)
	    // It follows from the first condition that c = 0. We solve a LSE for a and b
	    //    a*vmax^2 + b*vmax = N-1
	    //    a*vmax^2*(1-factor/(N-1))^2 + b*vmax*(1-factor/(N-1)) = N-2
	    
	    // solve by substituting b = (N-1 - a*vmax^2) / vmax
	    double tmp = 1 - factor/(N-1);
	    double b = ((N-2) - (N-1)*tmp*tmp)  /  (vMax*(tmp - tmp*tmp));
	    double a = ((N-1) - b*vMax) / (vMax*vMax);
	    
        return new double[]{a, b};
	}
	
	public double evalPolynomial(double x) {
		return coefficients[0]*x*x + coefficients[1]*x;
	}
	
	public double invertPolynomial(double y) {
		double a = coefficients[0];
		double b = coefficients[1];
		
		// NOTE: we always have that a > 0, because by construction p'(vmax) is steeper than the average 
		// steepness between p(0) and p(vmax).
		// It follows that there is one positive and one negative solution.
		// Since a > 0, the + solution is the positive one.
		return (-b + Math.sqrt(b*b + 4*a*y)) / (2*a);
		//return Math.max(-b + Math.sqrt(b*b + 4*a*y), -b - Math.sqrt(b*b + 4*a*y)) / (2*a);
	}
	
}
