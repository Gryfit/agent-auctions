package app;

import java.util.Map;

abstract class Auction {
    public AuctionType auctionType;

    public abstract Map<Resource, Double> initAction();
//    public abstract void onAuctionState
}
