package ch.uzh.ifi.ce.cabne.domains.LLLLGG.CCG;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import ch.uzh.ifi.ce.cabne.domains.Mechanism;
import ch.uzh.ifi.ce.cca.coreprices.constraintgeneration.ConstraintGenerationAlgorithm;
import ch.uzh.ifi.ce.cca.coreprices.pricingrules.Norm;
import ch.uzh.ifi.ce.cca.domain.Allocation;
import ch.uzh.ifi.ce.cca.domain.AuctionInstance;
import ch.uzh.ifi.ce.cca.domain.Bid;
import ch.uzh.ifi.ce.cca.domain.Bidder;
import ch.uzh.ifi.ce.cca.domain.BidderAllocation;
import ch.uzh.ifi.ce.cca.domain.BidderPayment;
import ch.uzh.ifi.ce.cca.domain.Bids;
import ch.uzh.ifi.ce.cca.domain.BundleBid;
import ch.uzh.ifi.ce.cca.domain.Good;
import ch.uzh.ifi.ce.cca.domain.Payment;
import ch.uzh.ifi.ce.cca.mechanisms.AuctionMechanism;
import ch.uzh.ifi.ce.cca.mechanisms.MechanismFactory;
import ch.uzh.ifi.ce.cca.mechanisms.ccg.ConfigurableCCGFactory;
import ch.uzh.ifi.ce.cca.mechanisms.ccg.NormFactory;
import ch.uzh.ifi.ce.cca.utils.CPLEXUtils;
import ch.uzh.ifi.ce.cca.utils.SolverMode;
import edu.harvard.econcs.jopt.solver.SolveParam;


public class LLLLGGQuadraticCCG implements Mechanism<Double[], Double[]> {
	List<Good> goods;
	List<Bidder> bidders;
	MechanismFactory mechanismFactory;

	public LLLLGGQuadraticCCG() {
		super();
		CPLEXUtils.SOLVER.initializeBNE();
		//CPLEXUtils.SOLVER.initializeCCG();
		CPLEXUtils.SOLVER.addSolveParam(SolverMode.GENERAL, SolveParam.THREADS, 1);
		
		mechanismFactory = new ConfigurableCCGFactory(
				new MidSizedDomainBlockingCoalitionFinder(), 
				new MidSizeVCGReferenceFactory(), 
				ImmutableList.of(
						NormFactory.withEqualWeights(Norm.MANHATTAN), 
						NormFactory.withEqualWeights(Norm.EUCLIDEAN)
						), 
				ImmutableSet.of(ConstraintGenerationAlgorithm.SEPARABILITY)
				);
		
		// create list of goods
		goods = new ArrayList<>();
		for (String g : Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H")) {
			goods.add(new Good(false, g));
		}
		goods.add(new Good(true, "dummy1"));
		goods.add(new Good(true, "dummy2"));
		
		// create bidders
		bidders = new ArrayList<>();
		// NOTE: bidders need exactly these names for FullMidSizeDomainWD to work
		for (String b : Arrays.asList("Yellow", "Green", "Blue", "DarkBlue", "Brown", "Red")) {
			bidders.add(new Bidder(b));
		}
		
	}

	public Bids getBids(Double[][] bids) {
		// local bidders
		BundleBid yellow1 = new BundleBid(BigDecimal.valueOf(bids[0][0]), ImmutableSet.of(goods.get(0), goods.get(1)), "Y1");
		BundleBid yellow2 = new BundleBid(BigDecimal.valueOf(bids[0][1]), ImmutableSet.of(goods.get(1), goods.get(2)), "Y2");
		BundleBid green1 = new BundleBid(BigDecimal.valueOf(bids[1][0]), ImmutableSet.of(goods.get(2), goods.get(3)), "G1");
		BundleBid green2 = new BundleBid(BigDecimal.valueOf(bids[1][1]), ImmutableSet.of(goods.get(3), goods.get(4)), "G2");
		BundleBid blue1 = new BundleBid(BigDecimal.valueOf(bids[2][0]), ImmutableSet.of(goods.get(4), goods.get(5)), "BL1");
		BundleBid blue2 = new BundleBid(BigDecimal.valueOf(bids[2][1]), ImmutableSet.of(goods.get(5), goods.get(6)), "BL2");
		BundleBid darkBlue1 = new BundleBid(BigDecimal.valueOf(bids[3][0]), ImmutableSet.of(goods.get(6), goods.get(7)), "DB1");
		BundleBid darkBlue2 = new BundleBid(BigDecimal.valueOf(bids[3][1]), ImmutableSet.of(goods.get(7), goods.get(0)), "DB2");

		// global bidders. Note that goods 8 and 9 are dummies
		BundleBid brown1 = new BundleBid(BigDecimal.valueOf(bids[4][0]), ImmutableSet.of(goods.get(0), goods.get(1), goods.get(2), goods.get(3), goods.get(8)), "Br1");
		BundleBid brown2 = new BundleBid(BigDecimal.valueOf(bids[4][1]), ImmutableSet.of(goods.get(4), goods.get(5), goods.get(6), goods.get(7), goods.get(8)), "Br2");
		BundleBid red1 = new BundleBid(BigDecimal.valueOf(bids[5][0]), ImmutableSet.of(goods.get(2), goods.get(3), goods.get(4), goods.get(5), goods.get(9)), "R1");
		BundleBid red2 = new BundleBid(BigDecimal.valueOf(bids[5][1]), ImmutableSet.of(goods.get(6), goods.get(7), goods.get(0), goods.get(1), goods.get(9)), "R2");
		
        Map<Bidder, Bid> map = new HashMap<>();
        map.put(bidders.get(0), new Bid(ImmutableSet.of(yellow1, yellow2)));
        map.put(bidders.get(1), new Bid(ImmutableSet.of(green1, green2)));
        map.put(bidders.get(2), new Bid(ImmutableSet.of(blue1, blue2)));
        map.put(bidders.get(3), new Bid(ImmutableSet.of(darkBlue1, darkBlue2)));
        map.put(bidders.get(4), new Bid(ImmutableSet.of(brown1, brown2)));
        map.put(bidders.get(5), new Bid(ImmutableSet.of(red1, red2)));
        
        return new Bids(map);
	}
	
	
	
	// TODO: these functions are just temporary, not actually required by algo. Need to remove
	public double[] getPayments(int i, Double[] v, Double[][] bids) {
		AuctionInstance auction = new AuctionInstance(getBids(bids), new HashSet<>(goods));
		AuctionMechanism mechanism = mechanismFactory.getMechanism(auction);
		Payment payment = mechanism.getPayment();
		double[] result = new double[6];
		for (int j=0; j<6;j++) {
			result[j] += payment.paymentOf(bidders.get(j)).getAmount().doubleValue();
		}
		return result;
	}
	public Allocation getAlloc(int i, Double[] v, Double[][] bids) {
		AuctionInstance auction = new AuctionInstance(getBids(bids), new HashSet<>(goods));
		AuctionMechanism mechanism = mechanismFactory.getMechanism(auction);
		
		// once the objects are created, get auction results
		return mechanism.getAllocation();
	}
	public AuctionInstance getAuction(int i, Double[] v, Double[][] bids) {
		return new AuctionInstance(getBids(bids), new HashSet<>(goods));
	}
	public Payment convertPayment(double[] payment) {
		Map<Bidder, BidderPayment> map = new HashMap<>();
		int i = 0;
		for (Bidder b : bidders) {
			map.put(b, new BidderPayment(BigDecimal.valueOf(payment[i++])));
		}
		return new Payment(map, null);
	}
	
	
	
	
	@Override
	public double computeUtility(int i, Double[] v, Double[][] bids) {
		AuctionInstance auction = new AuctionInstance(getBids(bids), new HashSet<>(goods));
		AuctionMechanism mechanism = mechanismFactory.getMechanism(auction);
		
		// once the objects are created, get auction results
		BidderAllocation alloc = mechanism.getAllocation().allocationOf(bidders.get(i));
		
		// if we win nothing, return 0.0 utility
		if (alloc == BidderAllocation.ZERO_ALLOCATION) {
			return 0.0;
		}
		
//		System.out.format("i=%d, v0=%4.3f v1=%4.3f\n", i, v[0], v[1]);
//		for (int j=0; j<6; j++) {
//			System.out.format("  j=%d, bj0=%4.3f bj1=%4.3f\n", j, bids[j][0], bids[j][1]);
//		}
		
		// if we win something, then compute value of allocation - core payment.
		double utility = - mechanism.getPayment().paymentOf(bidders.get(i)).getAmount().doubleValue();
		for (BundleBid b : alloc.getAcceptedBids()) {
			if (b.getId().endsWith("1")) {
				//System.out.format("  allocated bundle %s with value %s\n", b.getId(), v[0]);
				utility += v[0];
			}
			else {
				//System.out.format("  allocated bundle %s with value %s\n", b.getId(), v[1]);
				utility += v[1];
			}
		}
		
		return utility;
	}
	
}
