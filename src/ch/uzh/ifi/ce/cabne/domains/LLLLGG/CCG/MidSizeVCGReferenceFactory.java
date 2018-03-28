package ch.uzh.ifi.ce.cabne.domains.LLLLGG.CCG;

import ch.uzh.ifi.ce.cca.domain.Allocation;
import ch.uzh.ifi.ce.cca.domain.AuctionInstance;
import ch.uzh.ifi.ce.cca.domain.Payment;
import ch.uzh.ifi.ce.cca.mechanisms.ccg.ReferencePointFactory;

/**
 * Created by buenz on 14.01.16.
 */
public class MidSizeVCGReferenceFactory implements ReferencePointFactory {
    @Override
    public Payment computeReferencePoint(AuctionInstance auctionInstance, Allocation allocation) {
        return new FullMidSizeDomainWD(auctionInstance).getPayment();
    }

    @Override
    public String getName() {
        return "MIDSIZEVCG";
    }

    @Override
    public boolean belowCore() {
        return true;
    }
}
