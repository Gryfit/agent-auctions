package app;

import akka.actor.PoisonPill;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Auction {
    void onInitialiseEnvironment(ActorContext<Messages.RootMessages> actorContext, Map<Resource, Double> privateValues, BidHistory bidHistory,
                                 List<ActorRef<Messages.AuctionMessagesBidder>> bidders,
                                 Messages.InitialiseEnvironment msg);

    void onPlaceBid(ActorContext<Messages.RootMessages> actorContext, Map<Resource, Double> privateValues, BidHistory bidHistory,
                                               List<ActorRef<Messages.AuctionMessagesBidder>> bidders, Messages.PlaceBid msg);

}

class EnglishAuction implements Auction {
    private void proposeBidder(ActorContext<Messages.RootMessages> actorContext, List<ActorRef<Messages.AuctionMessagesBidder>> bidders, BidHistory bidHistory) {
//        System.out.println(getContext().getSelf().path() + " aa");
        Random r = new Random();
        ActorRef<Messages.AuctionMessagesBidder> bidder = bidders.get(r.nextInt(bidders.size()));
//        System.out.println("Proposing with " + bidHistory.getHighestBidPerResource().getOrDefault(Resource.r1, -1.0));
        bidder.tell(new Messages.AuctionState(bidHistory.getLastBid(), actorContext.getSelf()));
    }

    @Override
    public void onInitialiseEnvironment(ActorContext<Messages.RootMessages> actorContext, Map<Resource, Double> privateValues, BidHistory bidHistory, List<ActorRef<Messages.AuctionMessagesBidder>> bidders, Messages.InitialiseEnvironment msg) {
        int counter = 0;
        for (BiddingStrategy strategy : msg.strategies) {
            ActorRef<Messages.AuctionMessagesBidder> newBidder = actorContext.spawn(BidAgent.create(strategy), "Bidder_" + counter);
            newBidder.tell(new Messages.AnnounceAuction(AuctionType.English, privateValues));
            bidders.add(newBidder);
            counter++;
        }
        proposeBidder(actorContext, bidders, bidHistory);
    }

    @Override
    public void onPlaceBid(ActorContext<Messages.RootMessages> actorContext, Map<Resource, Double> privateValues, BidHistory bidHistory, List<ActorRef<Messages.AuctionMessagesBidder>> bidders, Messages.PlaceBid msg) {
        if (msg.bid.bidWith.isEmpty()) {
            msg.bid.actor.tell(new Messages.SendAuctionResult(AuctionResult.Lose, msg.bid.bidWith));
            actorContext.stop(msg.bid.actor);
            bidders.remove(msg.bid.actor);
            if (bidders.size() == 1) {
                bidders.get(0).tell(new Messages.SendAuctionResult(AuctionResult.Win, bidHistory.getLastBid()));
                actorContext.getSelf().unsafeUpcast().tell(PoisonPill.getInstance());
                return;
            }
        } else {
            bidHistory.addBid(msg.bid);
        }
        List<ActorRef<Messages.AuctionMessagesBidder>> newBidders = new LinkedList<>();
//        System.out.println(bidders.size());
        for (ActorRef<Messages.AuctionMessagesBidder> bidder : bidders) {
            if (bidder.path() != msg.bid.actor.path()) newBidders.add(bidder);
        }
//        System.out.println(newBidders.size());
        proposeBidder(actorContext, newBidders, bidHistory);
    }
}

class VickreyAuction implements Auction {

    @Override
    public void onInitialiseEnvironment(ActorContext<Messages.RootMessages> actorContext, Map<Resource, Double> privateValues, BidHistory bidHistory, List<ActorRef<Messages.AuctionMessagesBidder>> bidders, Messages.InitialiseEnvironment msg) {
        int counter = 0;
        for (BiddingStrategy strategy : msg.strategies) {
            ActorRef<Messages.AuctionMessagesBidder> newBidder = actorContext.spawn(BidAgent.create(strategy), "Bidder_" + counter);
            newBidder.tell(new Messages.AnnounceAuction(AuctionType.Vickrey, privateValues));
            newBidder.tell(new Messages.AuctionState(bidHistory.getLastBid(), actorContext.getSelf()));
            bidders.add(newBidder);
            counter++;
        }
    }

    @Override
    public void onPlaceBid(ActorContext<Messages.RootMessages> actorContext, Map<Resource, Double> privateValues, BidHistory bidHistory, List<ActorRef<Messages.AuctionMessagesBidder>> bidders, Messages.PlaceBid msg) {
//        System.out.println("got " + msg.bid.bidWith.getOrDefault(Resource.r1, 0.0));
        bidHistory.addBid(msg.bid);
        if (bidHistory.bids.size() == bidders.size() + 1)
        {
            Bid highest = bidHistory.getHighestBid(Resource.r1);
            Bid second = bidHistory.geSecondHighestBid(Resource.r1);
//            System.out.println("high " + highest.bidWith.getOrDefault(Resource.r1, 0.0));
//            System.out.println("sec " + second.bidWith.getOrDefault(Resource.r1, 0.0));
            highest.actor.tell(new Messages.SendAuctionResult(AuctionResult.Win, second.bidWith));
            for (ActorRef<Messages.AuctionMessagesBidder> bidder : bidders)
            {
                if (bidder.path() != highest.actor.path()) bidder.tell(new Messages.SendAuctionResult(AuctionResult.Lose, msg.bid.bidWith));
            }
            actorContext.getSelf().unsafeUpcast().tell(PoisonPill.getInstance());
        }
    }
}

class CombinatorialAuction implements Auction {

    private <A, B> List<Pair<A, B>> zip(List<A> as, List<B> bs) {
        return IntStream.range(0, Math.min(as.size(), bs.size()))
                .mapToObj(i -> new Pair<>(as.get(i), bs.get(i)))
                .collect(Collectors.toList());
    }

    private String padLeft(String s, int n) {
        return String.format("%" + n + "s", s).replace(' ', '0');
    }

    private int factorial(int n) {
        return IntStream.range(1, n+1).reduce(1, (i, j) -> i*j);
    }

    private List<Bid> setCover(List<Bid> lastBids) {
        if (lastBids.size() == 0) return lastBids;
        return IntStream
                .range(0, factorial(lastBids.size()))
                .mapToObj(Integer::toBinaryString)
                .map(s -> padLeft(s, lastBids.size()))
                .map(s -> s.chars().mapToObj(c -> c == ((int) '1')).collect(Collectors.toList()))
                .map(mask -> zip(mask, lastBids).stream().filter(Pair::getKey).map(Pair::getValue).collect(Collectors.toList()))
                .filter(bids -> bids.stream().reduce(new Bid(null, new HashMap<>()), Bid::addNoConflict).bidWith != null)
                .max(Comparator.comparingDouble(bids -> bids.stream().map(b -> b.sum).reduce(0.0, Double::sum)))
                .get();
    }

    @Override
    public void onInitialiseEnvironment(ActorContext<Messages.RootMessages> actorContext, Map<Resource, Double> privateValues, BidHistory bidHistory, List<ActorRef<Messages.AuctionMessagesBidder>> bidders, Messages.InitialiseEnvironment msg) {
        int counter = 0;
        for (BiddingStrategy strategy : msg.strategies) {
            ActorRef<Messages.AuctionMessagesBidder> newBidder = actorContext.spawn(BidAgent.create(strategy), "Bidder_" + counter);
            newBidder.tell(new Messages.AnnounceAuction(AuctionType.Combinatorial, privateValues));
            newBidder.tell(new Messages.AuctionState(privateValues, actorContext.getSelf()));
            bidders.add(newBidder);
            counter++;
        }
    }

    @Override
    public void onPlaceBid(ActorContext<Messages.RootMessages> actorContext, Map<Resource, Double> privateValues, BidHistory bidHistory, List<ActorRef<Messages.AuctionMessagesBidder>> bidders, Messages.PlaceBid msg) {
//        System.out.println("got " + msg.bid.bidWith.getOrDefault(Resource.r1, 0.0));
        bidHistory.addBid(msg.bid);
//        if (msg.bid.bidWith.isEmpty()) {
//            msg.bid.actor.tell(new Messages.SendAuctionResult(AuctionResult.Lose, msg.bid.bidWith));
//            return;
//        }

        if (bidHistory.bids.size() % bidders.size() == 0)
        {
            List<Bid> last = bidHistory.getLastBids(bidders.size()).stream().filter(e -> !e.bidWith.isEmpty()).collect(Collectors.toList());
            List<Bid> highest = setCover(last);
//            highest.forEach(b -> {
//                b.bidWith.forEach((r, d) -> System.out.print(r + ":" + d + " "));
//                System.out.print(";; ");
//            });
//            System.out.println("");
//            System.out.println(" highest " + highest.getOrDefault(Resource.r1, -1.0) + " " + privateValues.getOrDefault(Resource.r2, -1.0) + " " + privateValues.getOrDefault(Resource.r3, -1.0));

            bidHistory.putSuccessfulBids(highest);
            Map<Resource, Double> cumulative = bidHistory.getCumulativeBids().bidWith;
//            System.out.print("Cumulative ");
//            cumulative.forEach((r, d) -> System.out.print(r + ":" + d + " "));
//            System.out.println("");
            Map<Resource, Double> left = privateValues.entrySet().stream()
                    .filter(e -> !cumulative.containsKey(e.getKey()) || cumulative.getOrDefault(e.getKey(), 0.0) == 0.0)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

//            System.out.print("Left ");
//            left.forEach((r, d) -> System.out.print(r + ":" + d + " "));
//            System.out.println("");
            if (left.isEmpty()) {
                List<Bid> successful = bidHistory.getBidsPerAgent();
                for (Bid bid: successful) bid.actor.tell(new Messages.SendAuctionResult(AuctionResult.Win, bid.bidWith));
                for (ActorRef<Messages.AuctionMessagesBidder> bidder: bidders)
                    bidder.tell(new Messages.SendAuctionResult(AuctionResult.Lose, msg.bid.bidWith));
                actorContext.getSelf().unsafeUpcast().tell(PoisonPill.getInstance());
            }
            else {
                for (ActorRef<Messages.AuctionMessagesBidder> bidder: bidders) {
                    Double toll = highest.stream().filter(b -> b.actor.path() == bidder.path()).findFirst().map(b -> b.sum).orElse(0.0);
                    bidder.tell(new Messages.AuctionState(left, actorContext.getSelf(), toll));
                }
            }
        }
    }
}
