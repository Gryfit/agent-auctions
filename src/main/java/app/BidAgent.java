package app;

import akka.actor.Kill;
import akka.actor.PoisonPill;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BidAgent extends AbstractBehavior<Messages.AuctionMessagesBidder> {

    private  Map<Resource, Double> resources = new HashMap<>();
    private Map<Resource, Double> privateValues = new HashMap<>();
    private final BiddingStrategy biddingStrategy;

    public BidAgent(ActorContext<Messages.AuctionMessagesBidder> context, BiddingStrategy biddingStrategy) {
        super(context);
//        System.out.println("BIDDER CREATED");
        this.biddingStrategy = biddingStrategy;
        Stream
                .of(Resource.values())
                .forEach(k -> this.resources.put(k, Math.random() * 300));
//        System.out.println("res " + biddingStrategy.toString() + " " + resources.get(Resource.r1));
    }

    public static Behavior<Messages.AuctionMessagesBidder> create(BiddingStrategy biddingStrategy) {
        return Behaviors.setup(context -> new BidAgent(context, biddingStrategy));
    }

    @Override
    public Receive<Messages.AuctionMessagesBidder> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.AnnounceAuction.class, this::onAnnounceAuction)
                .onMessage(Messages.AuctionState.class, this::onAuctionState)
                .onMessage(Messages.SendAuctionResult.class, this::onAuctionResult)
//                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<Messages.AuctionMessagesBidder> onAnnounceAuction(Messages.AnnounceAuction msg){
        privateValues = msg.resources.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() + (Math.random() * 25 - 12)));
//        System.out.println(getContext().getSelf().path() + " 1 " + privateValues.get(Resource.r1));
//        System.out.println("privV " + biddingStrategy.toString() + " " + privateValues.get(Resource.r1));
        return this;
    }

//    private Behavior<Messages.AuctionMessagesBidder> onPostStop() {
//        getContext().getSystem().log().info("Master Control Program stopped");
//        return this;
//    }

    private Behavior<Messages.AuctionMessagesBidder> onAuctionState(Messages.AuctionState msg) {
//        System.out.println(getContext().getSelf().path() + " 2 " + msg.highestBids.get(Resource.r1));
        Map<Resource, Double> newBid = biddingStrategy.newBid(privateValues, msg)
                .entrySet().stream()
//                .peek(e -> System.out.println("aa "+ e.getValue() + " " + resources.getOrDefault(e.getKey(), 0.0)))
                .filter(e -> e.getValue() <= resources.getOrDefault(e.getKey(), 0.0))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        System.out.println("Bidding with " + biddingStrategy.toString() + " " + newBid.getOrDefault(Resource.r1, -1.0));
        Bid bid = new Bid(getContext().getSelf(), newBid);
        msg.replyTo.tell(new Messages.PlaceBid(bid));
        return this;
    }

    private Behavior<Messages.AuctionMessagesBidder> onAuctionResult(Messages.SendAuctionResult msg){
//        System.out.println(getContext().getSelf().path() + " 3");
        if (msg.result.equals(AuctionResult.Win))
        {
            System.out.print("win " + biddingStrategy.toString());
            msg.toPay.entrySet().stream().map(e -> privateValues.get(e.getKey()) - e.getValue()).forEach(p -> System.out.print(" " + p));
        System.out.println("");
        }
        getContext().getSelf().unsafeUpcast().tell(PoisonPill.getInstance());
//        getContext().getSelf().tell(Kill.getInstance());
        return this;
    }

}
