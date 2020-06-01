package app;

import akka.actor.PoisonPill;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        bidder.tell(new Messages.AuctionState(bidHistory.getHighestBidPerResource(), actorContext.getSelf()));
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
                bidders.get(0).tell(new Messages.SendAuctionResult(AuctionResult.Win, bidHistory.getHighestBidPerResource()));
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
