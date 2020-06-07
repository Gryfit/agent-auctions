package app;

import akka.actor.PoisonPill;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.*;
import java.util.stream.Stream;


public class RootAgent extends AbstractBehavior<Messages.RootMessages> {

    private BidHistory bidHistory = new BidHistory();
    private List<ActorRef<Messages.AuctionMessagesBidder>> bidders = new LinkedList<>();
    private Map<Resource, Double> privateValues;
    private Auction auction;

    public RootAgent(ActorContext<Messages.RootMessages> context, Auction auction) {
        super(context);
        this.auction = auction;
//        System.out.println("AUCTION CREATED");
    }

    public static Behavior<Messages.RootMessages> create(Auction auction) {
        return Behaviors.setup(context -> new RootAgent(context, auction));
    }


    @Override
    public Receive<Messages.RootMessages> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.InitialiseEnvironment.class, this::onInitialiseEnvironment)
                .onMessage(Messages.PlaceBid.class, this::onPlaceBid)
                .build();
    }

    private Behavior<Messages.RootMessages> onInitialiseEnvironment(Messages.InitialiseEnvironment msg){
        privateValues = new HashMap<>();
        Stream.of(Resource.values()).forEach(r -> privateValues.put(r, Math.random() * 100));
//        privateValues.put(Resource.r1, Math.random() * 100);
//        System.out.println(getContext().getSelf().path() + " cc " + privateValues.getOrDefault(Resource.r1, -1.0) + " " + privateValues.getOrDefault(Resource.r2, -1.0) + " " + privateValues.getOrDefault(Resource.r3, -1.0));
        auction.onInitialiseEnvironment(getContext(), privateValues, bidHistory, bidders, msg);
//        System.out.println("init " + privateValues.getOrDefault(Resource.r1, -1.0));
        return this;
    }


    private Behavior<Messages.RootMessages> onPlaceBid(Messages.PlaceBid msg){
//        System.out.println(msg.bid.bidWith.getOrDefault(Resource.r1, -1.0));
//        System.out.println(getContext().getSelf().path() + " bb " + msg.bid.bidWith.getOrDefault(Resource.r1, -1.0) + " " + msg.bid.bidWith.getOrDefault(Resource.r2, -1.0) + " " + msg.bid.bidWith.getOrDefault(Resource.r3, -1.0));
        auction.onPlaceBid(getContext(), privateValues, bidHistory, bidders, msg);
        return this;
    }
}
