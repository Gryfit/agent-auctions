package app;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.*;


public class RootAgent extends AbstractBehavior<Messages.RootMessages> {

    private BidHistory bidHistory = new BidHistory();
    private List<ActorRef<Messages.AuctionMessagesBidder>> bidders = new LinkedList<>();
    private Map<Resource, Double> privateValues;

    public RootAgent(ActorContext<Messages.RootMessages> context) {
        super(context);
//        System.out.println("AUCTION CREATED");
    }

    public static Behavior<Messages.RootMessages> create() {
        return Behaviors.setup(RootAgent::new);
    }


    @Override
    public Receive<Messages.RootMessages> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.InitialiseEnvironment.class, this::onInitialiseEnvironment)
//                .onMessage(Messages.JoinAuction.class, this::onJoinAuction)
                .onMessage(Messages.PlaceBid.class, this::onPlaceBid)
                .build();
    }

    private Behavior<Messages.RootMessages> onInitialiseEnvironment(Messages.InitialiseEnvironment msg){
        privateValues = new HashMap<>();
        privateValues.put(Resource.r1, Math.random() * 100);
//        System.out.println(getContext().getSelf().path() + " cc " + privateValues.get(Resource.r1));
        int counter = 0;
        for (BiddingStrategy strategy: msg.strategies) {
            ActorRef<Messages.AuctionMessagesBidder> newBidder = getContext().spawn(BidAgent.create(strategy), "Bidder_"+counter);
            newBidder.tell(new Messages.AnnounceAuction(AuctionType.English, privateValues));
            bidders.add(newBidder);
            counter++;
        }
        proposeBidder(bidders);
        return this;
    }

    private void proposeBidder(List<ActorRef<Messages.AuctionMessagesBidder>> bidds) {
//        System.out.println(getContext().getSelf().path() + " aa");
        Random r = new Random();
        ActorRef<Messages.AuctionMessagesBidder> bidder = bidds.get(r.nextInt(bidds.size()));
        bidder.tell(new Messages.AuctionState(bidHistory.getHighestBidPerResource(), getContext().getSelf()));
    }

//    private Behavior<Messages.RootMessages> onJoinAuction(Messages.JoinAuction msg){
//        bidders.add(msg.ref);
//        // LOGIC
//        return this;
//    }

    private Behavior<Messages.RootMessages> onPlaceBid(Messages.PlaceBid msg){
//        System.out.println(getContext().getSelf().path() + " bb " + msg.bid.bidWith.get(Resource.r1));

        if (msg.bid.bidWith.isEmpty()) {
            msg.bid.actor.tell(new Messages.SendAuctionResult(AuctionResult.Lose));
            getContext().stop(msg.bid.actor);
            bidders.remove(msg.bid.actor);
            if (bidders.size() == 1) {
                bidders.get(0).tell(new Messages.SendAuctionResult(AuctionResult.Win));
                return this;
            }
        }
        else {
            this.bidHistory.addBid(msg.bid);
        }
        List<ActorRef<Messages.AuctionMessagesBidder>> newBidders = new LinkedList<>();
        for (ActorRef<Messages.AuctionMessagesBidder> bidder : bidders) {
            if (bidder != msg.bid.actor) newBidders.add(bidder);
        }
        proposeBidder(newBidders);
        return this;
    }
}
