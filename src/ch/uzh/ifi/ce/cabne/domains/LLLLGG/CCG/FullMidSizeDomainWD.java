package ch.uzh.ifi.ce.cabne.domains.LLLLGG.CCG;

import ch.uzh.ifi.ce.cca.NumericalAllocation;
import ch.uzh.ifi.ce.cca.domain.*;
import ch.uzh.ifi.ce.cca.mechanisms.AuctionMechanism;
import ch.uzh.ifi.ce.cca.mechanisms.MetaInfo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by buenz on 11.01.16.
 */
public class FullMidSizeDomainWD implements AuctionMechanism {
    private final AuctionInstance auctionInstance;
    private Allocation allocation;
    private static final Bidder yellow = new Bidder("Yellow");
    private static final Bidder green = new Bidder("Green");
    private static final Bidder blue = new Bidder("Blue");
    private static final Bidder darkBlue = new Bidder("DarkBlue");
    private static final Bidder brown = new Bidder("Brown");
    private static final Bidder red = new Bidder("Red");
    private static final List<Bidder> bidderList = ImmutableList.of(yellow, green, blue, darkBlue, brown, red);

    private final MidSizeDomainWD wd = new MidSizeDomainWD();
    private NumericalAllocation numericalAllocation = null;

    public FullMidSizeDomainWD(AuctionInstance auctionInstance) {
        this.auctionInstance = auctionInstance;
    }

    @Override
    public Allocation getAllocation() {
        if (allocation == null) {
            int index = 0;
            BigDecimal[] bidArray = new BigDecimal[12];
            for (Bidder bidder : bidderList) {
                Bid bid = auctionInstance.getBid(bidder);
                for (BundleBid bundleBid : bid.getBundleBids()) {
                    if (bundleBid.getId().endsWith("1")) {
                        bidArray[index] = bundleBid.getAmount();
                    } else {
                        bidArray[index + 1] = bundleBid.getAmount();

                    }
                }
                index += 2;
            }
            numericalAllocation = wd.solveWD(bidArray);
            Map<Bidder, BidderAllocation> allocationMap = new HashMap<>(numericalAllocation.getAllocation().length);
            BigDecimal totalValue = BigDecimal.ZERO;
            for (int winningBid : numericalAllocation.getAllocation()) {
                Bidder winningBidder = bidderList.get(winningBid / 2);
                BundleBid winningBundleBid = null;
                Bid bid = auctionInstance.getBid(winningBidder);
                for (BundleBid bundleBid : bid.getBundleBids()) {
                    {
                        String suffix = String.valueOf(winningBid % 2 + 1);
                        if (bundleBid.getId().endsWith(suffix)) {
                            winningBundleBid = bundleBid;
                            break;
                        }
                    }
                }

                BidderAllocation bidderAllocation = new BidderAllocation(winningBundleBid.getAmount(), winningBundleBid.getBundle(), ImmutableSet.of(winningBundleBid));
                allocationMap.put(winningBidder, bidderAllocation);
                totalValue = totalValue.add(winningBundleBid.getAmount());

            }
            this.allocation = new Allocation(totalValue, allocationMap, auctionInstance.getBids(), new MetaInfo());
        }
        return allocation;
    }

    @Override
    public AuctionResult getAuctionResult() {
        getAllocation();
        BigDecimal[] payment = wd.computeVCG(numericalAllocation);
        int index = 0;
        Map<Bidder, BidderPayment> paymentMap = new HashMap<>(payment.length);
        for (int winningBid : numericalAllocation.getAllocation()) {
            Bidder winningBidder = bidderList.get(winningBid / 2);
            BidderPayment bidderPayment = new BidderPayment(payment[index++]);
            paymentMap.put(winningBidder, bidderPayment);
        }
        return new AuctionResult(new Payment(paymentMap, new MetaInfo()), allocation);
    }
}
