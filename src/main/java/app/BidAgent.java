package app;

import akka.actor.Kill;
import akka.actor.PoisonPill;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import javafx.util.Pair;

import java.util.*;
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
        resources.put(Resource.r1, Math.random() * 100 + 50);
//        System.out.println("res " + biddingStrategy.toString() + " " + getContext().getSelf().path() + " " + resources.get(Resource.r1));
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
//        System.out.println(getContext().getSelf().path() + " 1 " + privateValues.getOrDefault(Resource.r1, -1.0) + " " + privateValues.getOrDefault(Resource.r2, -1.0) + " " + privateValues.getOrDefault(Resource.r3, -1.0));
//        System.out.println("privV " + biddingStrategy.toString() + " " + privateValues.get(Resource.r1));
        return this;
    }

//    private Behavior<Messages.AuctionMessagesBidder> onPostStop() {
//        getContext().getSystem().log().info("Master Control Program stopped");
//        return this;
//    }

    private Behavior<Messages.AuctionMessagesBidder> onAuctionState(Messages.AuctionState msg) {

//        System.out.println(getContext().getSelf().path() + " 2 " + msg.agentsToll);
        resources.replace(Resource.r1, resources.get(Resource.r1) - msg.agentsToll);
//        System.out.println(getContext().getSelf().path() + " 2.1 " + resources.getOrDefault(Resource.r1, -1.0));


        Map<Resource, Double> prop = biddingStrategy.newBid(privateValues, msg);
//        System.out.println(getContext().getSelf().path() + " Propos with " + biddingStrategy.toString() + " " + prop.getOrDefault(Resource.r1, -1.0) + " " + prop.getOrDefault(Resource.r2, -1.0) + " " + prop.getOrDefault(Resource.r3, -1.0));

        Map<Resource, Double> newBid = new HashMap<>();
        List<Map.Entry<Resource, Double>> tmp = new ArrayList<>(prop.entrySet());
        Collections.shuffle(tmp);
        tmp.stream()
                .takeWhile(e -> {
                    newBid.put(e.getKey(), e.getValue());
                    return newBid.values().stream().reduce(0.0, Double::sum) < resources.getOrDefault(Resource.r1, 0.0);
                }).forEach(e -> {});
//        System.out.println(getContext().getSelf().path() + " Resol with " + biddingStrategy.toString() + " " + newBid.getOrDefault(Resource.r1, -1.0) + " " + newBid.getOrDefault(Resource.r2, -1.0) + " " + newBid.getOrDefault(Resource.r3, -1.0));
        Map<Resource, Double> newBid2 = newBid
                .entrySet().stream()
                .filter(e -> e.getValue() > 0.0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        System.out.println(getContext().getSelf().path() + " Bidding with " + biddingStrategy.toString() + " " + newBid2.getOrDefault(Resource.r1, -1.0) + " " + newBid2.getOrDefault(Resource.r2, -1.0) + " " + newBid2.getOrDefault(Resource.r3, -1.0));
        Bid bid = new Bid(getContext().getSelf(), newBid2);
        msg.replyTo.tell(new Messages.PlaceBid(bid));
        return this;
    }

    private Behavior<Messages.AuctionMessagesBidder> onAuctionResult(Messages.SendAuctionResult msg){
//        System.out.println(getContext().getSelf().path() + " 3");
        if (msg.result.equals(AuctionResult.Win))
        {
            System.out.print("win " + biddingStrategy.toString() + " " + msg.toPay.entrySet().stream().map(e -> privateValues.get(e.getKey()) - e.getValue()).reduce(0.0, Double::sum) + "\n");
        }
        getContext().getSelf().unsafeUpcast().tell(PoisonPill.getInstance());
//        getContext().getSelf().tell(Kill.getInstance());
        return this;
    }

}
