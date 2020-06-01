package app;

import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


public interface BiddingStrategy {
    Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState);

}

class SmallStepsStrategy implements BiddingStrategy {
    @Override
    public String toString() {
        return "small-steps";
    }

    @Override
    public Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState) {
        return auctionState.highestBids.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() + 0.01));
    }

}

class BigStepsStrategy implements BiddingStrategy {
    @Override
    public String toString() {
        return "big-steps";
    }

    @Override
    public Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState) {
        return auctionState.highestBids.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() + 8));
    }
}

class RoundToIntegerStrategy implements BiddingStrategy {
    @Override
    public String toString() {
        return "round-to-integer";
    }

    @Override
    public Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState) {
        return auctionState.highestBids.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Double.valueOf(e.getValue().intValue()+1)));
    }

}

class ExactlyPrivateValueStrategy implements BiddingStrategy {
    @Override
    public String toString() {
        return "exactly-private-value";
    }

    @Override
    public Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState) {
        return privateValues;
    }

}

class LowerPrivateValueStrategy implements BiddingStrategy {
    @Override
    public String toString() {
        return "lower-private-value";
    }

    @Override
    public Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState) {
        return privateValues.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() - Math.random()*30));
    }

}

class HigherPrivateValueStrategy implements BiddingStrategy {
    @Override
    public String toString() {
        return "higher-private-value";
    }

    @Override
    public Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState) {
        return privateValues.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() + Math.random()*30));
    }

}