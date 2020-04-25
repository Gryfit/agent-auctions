package app;


import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.LinkedList;


enum AuctionType {
    English,
}

enum Resource {
    r1
}

enum AuctionResult {
    Win,
    Lose,
}

final class Bid {
    public final ActorRef actor;
    public final Resource bidOn;
    public final HashMap<Resource, Double> bidWith;

    public Bid(ActorRef actor, Resource bidOn, HashMap<Resource, Double> bidWith) {
        this.actor = actor;
        this.bidOn = bidOn;
        this.bidWith = bidWith;
    }
}

/**
 *  In event sourcing manner - keep event stream here and process on demand
 */
final class BidHistory {
    public LinkedList<Bid> bids = new LinkedList<>();

    public BidHistory() { }

    public void addBid(Bid bid) {
        this.bids.add(bid);
    }

    public HashMap<Resource, Bid> getHighestBidPerResource(){
        return new HashMap<>();
    }
}

