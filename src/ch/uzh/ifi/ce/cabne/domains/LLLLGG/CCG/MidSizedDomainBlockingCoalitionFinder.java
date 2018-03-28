package ch.uzh.ifi.ce.cabne.domains.LLLLGG.CCG;

import ch.uzh.ifi.ce.cca.domain.Allocation;
import ch.uzh.ifi.ce.cca.domain.AuctionInstance;
import ch.uzh.ifi.ce.cca.domain.AuctionResult;
import ch.uzh.ifi.ce.cca.domain.BlockingAllocation;
import ch.uzh.ifi.ce.cca.mechanisms.Allocator;
import ch.uzh.ifi.ce.cca.mechanisms.BlockingAllocationFinder;

public class MidSizedDomainBlockingCoalitionFinder implements BlockingAllocationFinder {
    /**
     *
     */


    @Override
    public BlockingAllocation findBlockingAllocation(AuctionInstance auctionInstance, AuctionResult priorResult) {
        AuctionInstance reducedAuction = auctionInstance.reducedBy(priorResult);
        Allocator blockingCoalitionFinder = new FullMidSizeDomainWD(reducedAuction);
        Allocation allocation = blockingCoalitionFinder.getAllocation();
        return BlockingAllocation.of(allocation);
    }

}
