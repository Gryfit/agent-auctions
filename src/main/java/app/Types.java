package app;


import akka.actor.typed.ActorRef;

import java.util.HashMap;
import java.util.Map;
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
    public final ActorRef<Messages.AuctionMessagesBidder> actor;
//    public final Resource bidOn;
    public final Map<Resource, Double> bidWith;

    public Bid(ActorRef<Messages.AuctionMessagesBidder> actor, Map<Resource, Double> bidWith) {
        this.actor = actor;
//        this.bidOn = bidOn;
        this.bidWith = bidWith;
    }
}

/**
 *  In event sourcing manner - keep event stream here and process on demand
 */
final class BidHistory {
    public LinkedList<Bid> bids = new LinkedList<>();

    public BidHistory() {
        Map<Resource, Double> first = new HashMap<>();
        first.put(Resource.r1, 0.0);
        bids.add(new Bid(null, first));
    }

    public void addBid(Bid bid) {
        this.bids.add(bid);
    }

    public Map<Resource, Double> getHighestBidPerResource(){
        return bids.getLast().bidWith;
    }
}

