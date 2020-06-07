package app;


import akka.actor.typed.ActorRef;

import java.util.*;
import java.util.stream.Collectors;


enum AuctionType {
    English,
    Vickrey,
    Combinatorial
}

enum Resource {
    r1,
    r2,
    r3
}

enum AuctionResult {
    Win,
    Lose,
}

final class Bid {
    public final ActorRef<Messages.AuctionMessagesBidder> actor;
//    public final Resource bidOn;
    public final Map<Resource, Double> bidWith;
    public final Double sum;

    public Bid(ActorRef<Messages.AuctionMessagesBidder> actor, Map<Resource, Double> bidWith) {
        this.actor = actor;
        this.bidWith = bidWith;
        if (bidWith != null) this.sum = bidWith.values().stream().reduce(0.0, Double::sum);
        else this.sum = 0.0;
    }

    public Bid addNoConflict(Bid that) {
        if (bidWith == null) return this;
        if (bidWith.keySet().stream().anyMatch(that.bidWith::containsKey))
            return new Bid(null, null);
        else return add(that);
    }

    public static Bid identity() {
        Map<Resource, Double> first = new HashMap<>();
        Arrays.stream(Resource.values()).forEach(r -> first.put(r, 0.0));
        return new Bid(null, first);
    }



    public Bid add(Bid that) {
        Map<Resource, Double> newResources = new HashMap<>(this.bidWith);
        that.bidWith.forEach((key, value) -> newResources.merge(key, value, Double::sum));
        ActorRef<Messages.AuctionMessagesBidder> actor = (this.actor != null) ? this.actor : that.actor;
        return new Bid(actor, newResources);
    }
}

/**
 *  In event sourcing manner - keep event stream here and process on demand
 */
final class BidHistory {
    public LinkedList<Bid> bids = new LinkedList<>();
    public List<Bid> successfulBids = new LinkedList<>();

    public BidHistory() {
    }

    public void addBid(Bid bid) {
        this.bids.add(bid);
    }

    public Map<Resource, Double> getLastBid(){
        return bids.getLast().bidWith;
    }

    public List<Bid> getLastBids(int n) {
        return bids.stream()
                .skip(bids.size() - n)
                .collect(Collectors.toList());
    }

    public Bid getHighestBid(Resource r) {
        Comparator<Bid> byResource = Comparator.comparing(bid -> bid.bidWith.getOrDefault(r, 0.0));
        return bids.stream().max(byResource).get();
    }

    public Bid geSecondHighestBid(Resource r) {
        Comparator<Bid> byResource = Comparator.comparing(bid -> bid.bidWith.getOrDefault(r, 0.0));
        return bids.stream().sorted(byResource.reversed()).skip(1).findFirst().get();
    }

    public void putSuccessfulBids(List<Bid> bids) {
        this.successfulBids.addAll(bids);
    }

    public Bid getCumulativeBids() {
        return successfulBids.stream().reduce(Bid.identity(), Bid::add);
    }

    public List<Bid> getBidsPerAgent() {
        return successfulBids.stream()
                .collect(Collectors.groupingBy(b -> b.actor.path(), Collectors.mapping((Bid b) -> b, Collectors.toList())))
                .values().stream()
                .map(bidList -> bidList.stream().reduce(Bid.identity(), Bid::add))
                .collect(Collectors.toList());
    }
}

