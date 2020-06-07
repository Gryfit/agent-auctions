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
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() - Math.random()*5));
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
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() + Math.random()*5));
    }

}

class RandomStrategy implements BiddingStrategy {
    @Override
    public String toString() {
        return "random";
    }

    @Override
    public Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState) {
        return privateValues.entrySet().stream()
                .filter(e -> auctionState.highestBids.containsKey(e.getKey()))
                .filter(e -> Math.random() <= 0.6)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() * Math.random()));
    }

}

class ImpatientStrategy implements BiddingStrategy {
    @Override
    public String toString() {
        return "impatient";
    }

    @Override
    public Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState) {
        return privateValues.entrySet().stream()
                .filter(e -> auctionState.highestBids.containsKey(e.getKey()))
                .filter(e -> Math.random() <= 0.8)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
    }

}

class MaximizationStrategy implements BiddingStrategy {
    @Override
    public String toString() {
        return "maximization";
    }

    @Override
    public Map<Resource, Double> newBid(Map<Resource, Double> privateValues, Messages.AuctionState auctionState) {
        return privateValues.entrySet().stream()
                .filter(e -> auctionState.highestBids.containsKey(e.getKey()))
                .filter(e -> Math.random() <= 0.4)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() * 0.5));
    }

}