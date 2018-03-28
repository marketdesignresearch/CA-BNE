# CA-BNE


This is a piece of software used for numerically computing approximate Bayes-Nash equilibria (ε-BNEs) of combinatorial auctions. 

Our algorithm is described in the paper "Computing Bayes-Nash Equilibria in Combinatorial Auctions with Continuous Value and Action Spaces", by Vitor Bosshard, Benedikt Bünz, Benjamin Lubin and Sven Seuken.

Note: this is a private beta release! If you have been given access to this code, please do not share it further for the moment. The code will be made available publicly together with the definitive version of our paper, sometime in the first half of 2018.


## Installation



## Example 1: LLG Quadratic

To demonstrate how to configure our algorithm, we include an example of the LLG domain, where there are two local bidders, each of which has a valuations drawn u.a.r. from [0,1] for one of two distinct items, and a global bidder, with a value drawn u.a.r. from [0,2] for the bundle of both items. The payment rule is quadratic, a.k.a. VCG-nearest.

First, we create a "BNESolverContext" object, which will contain all the objects making up our algorithm, and read in a configuration file. This class, as well as almost all other classes, is instanciated with two generic parameters, corresponding to the representation of values and bids.

>  BNESolverContext<Double, Double> context = new BNESolverContext<>();
>  String configfile = args[0];
>  context.parseConfig(configfile);

Then, we add all the pieces needed to specify the algorithm, from the way best responses are computed to how the strategies are updated.

>  context.setOptimizer(new PatternSearch<Double, Double>(context, new UnivariatePattern()));
>  context.setIntegrator(new MCIntegrator<Double, Double>(context));
>  context.setRng(2, new CommonRandomGenerator(2));
>  context.setUpdateRule(new UnivariateDampenedUpdateRule(0.2, 0.7, 0.5 / context.getDoubleParameter("epsilon"), true));
>  context.setBRC(new AdaptivePWLBRCalculator(context));
>  context.setOuterBRC(new PWLBRCalculator(context));
>  context.setVerifier(new ExactUnivariateVerifier(context));

Then, we add the objects representing the auction setting

>  context.setMechanism(new Quadratic());
>  context.setSampler(new UniformLLGSampler(context));

We instanciate the BNE algorithm for an auction with 3 players and the given context.

>  BNEAlgorithm<Double, Double> bneAlgo = new BNEAlgorithm<>(3, context);

We set the initial strategies for the bidders to be truthful. Bidder 1 (the second local bidder) is in a position symmetric to bidder 0 (the first local bidder), so we make him play bidder 0's strategy. The global bidder is known to bid truthful [Beck and Ott, 2013], so we don't update his strategy. This results in the algorithm only updating bidder 0's strategy each round.

> bneAlgo.setInitialStrategy(0, UnivariatePWLStrategy.makeTruthful(0.0, 1.0));
> bneAlgo.setInitialStrategy(2, UnivariatePWLStrategy.makeTruthful(0.0, 2.0));
> bneAlgo.makeBidderSymmetric(1, 0);
> bneAlgo.makeBidderNonUpdating(2);

To actually see what's going on during the algorithm's execution, we implement a special callback interface that gets invoked after each iteration. In this example, we will just output the intermediate strategies computed for bidder 0, along with the estimated epsilon.

> BNEAlgorithmCallback<Double, Double> callback = new BNEAlgorithmCallback<Double, Double>() {
>	@Override
>	public void afterIteration(int iteration, BNEAlgorithm.IterationType type, List<Strategy<Double, Double>> strategies, double epsilon) {
>		StringBuilder builder = new StringBuilder();
>		builder.append(String.format("%2d", iteration));
>		builder.append(String.format(" %7.6f  ", epsilon));
>
>		UnivariatePWLStrategy sPWL = (UnivariatePWLStrategy) strategies.get(0);
>		for (Map.Entry<Double, Double> e : sPWL.getData().entrySet()) {
>			builder.append(String.format("%7.6f",e.getKey()));
>			builder.append(" ");
>			builder.append(String.format("%7.6f",e.getValue()));
>			builder.append("  ");
>		}
>		System.out.println(builder.toString());
> 	}
> };
> bneAlgo.setCallback(callback);

Finally, we run the algorithm

> bneAlgo.run();

The full example can be found [here](src/ch/uzh/ifi/ce/cabne/examples/LLGQuadratic.java). Its output can be visualized using the following [Python script](scripts/llg_anim_BNE.py). The computed BNE should look like this:

[TODO image]

## Implementing your own Auctions

In order to compute an equilibrium for your own domain and payment rule, there are two interfaces you need to implement: a BidSampler and a Mechanism.

The BidSampler interface provides an iterator that yields samples of bid profiles drawn according to the value distributions and strategies of all bidders. A bid profile is represented as an array of the generic 'Bid' type. Within this class, one can add integration tricks such as importance sampling as well.

The Mechanism interface implements the mapping from a valuation v_i and a bid profile b all the way to a bidder's utility u_i.
This is slightly unintuitive, since one would expect an auction mechanism to consist of an allocation and a payment rule, with the output of these rules being fed into some explicit utility function. However, the way it is implemented makes sense from a computational perspective. We are avoiding the creation of an expensive object representing the allocation, which in most cases would just be passed to the utility function where it is read once, then destroyed. This way, the class implementing the Mechanism interface is free to internally represent the auction outcome however it sees fit.

Note that the BidSampler will want to make use of a random number generator, so you need to make sure that one with the correct dimensionality is added to the context. The dimension will typically be the total number of bundles all bidders except i are interested in.

## Example 2: LLLLGG

Next, we consider a larger example, where we find a BNE for the first price rule in the LLLLGG domain. This code is very similar to example 1. The main difference is that Values and Bids are multidimensional, implemented as Double[]. The BidSampler and Mechanism implementations are responsible themselves to interpret these arrays of raw data in a consistent way. This domain could also be implemented by writing Java classes representing Values and Bids, but this would make the code slower. All algorithm pieces are provided in a variant supporting multiple dimensions.

In addition to the first price rule, our code also provides an implementation of quadratic and other core-selecting payment rules in LLLLGG, but it should be noted that quadratic requires a quadratic program (QP) solver such as CPLEX to be installed.

The callback function writes out a file representing the strategy at each iteration. The code for this example can be found here [TODO: link]. The progress of the algorithm can be visualized with the help of another Python script [TODO: link]. The approximate equilibrium should look like this:

[TODO image]


## Example 3: LLG First Price

As a final example, we want to find a BNE for first price in LLG. This is harder than quadratic from example 1 because the global player is not truthful anymore.


For this example, we've had some issues with oscillating behaviour around the equilibrium that prevents us from converging.

To solve this issue, we force the pattern search to stay in a smaller and smaller neighborhoods around the current strategy as iterations pass. This is implemented by adding a couple lines of code to the callback function, which change the pattern search settings on the fly:

> double temperature = Math.pow(0.7, Math.max(0.0, iteration - 5));
>	if (type != BNEAlgorithm.IterationType.INNER) {
>		temperature = 1.0;
>	}
>	patternSearch.setInitialScale(0.01+0.99*temperature);

Note that we turn off this behaviour in the verification step, so that the epsilon output by the algorithm is computed considering all possible alternative bids.

The code can be found here [TODO link] and the script to visualize results is here [TODO link]. The resulting BNE should look like this:

[TODO image]





## Beta Notes

There are a few thing missing from this beta release that will be added in the public release
* A generic auction builder, where you only specify the auction topology in an abstract way, and an implementation of the required interfaces is automatically generated.
* The full code used to run the experiments in our paper.
* The scripts we used to distribute the running of the algorithm over our compute cluster. While this only runs on our hardware, it demonstrates how to implement a parallel version of the BNEAlgorithm class.


 








