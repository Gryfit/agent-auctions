package app;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.stream.Stream;


public class BidAgent extends AbstractBehavior<Messages.AuctionMessagesBidder>  {

    public final HashMap<Resource, Double> resources = new HashMap<>();


    public BidAgent(ActorContext<Messages.AuctionMessagesBidder> context) {
        super(context);
        System.out.println("BIDDER CREATED");
        Stream
            .of(Resource.values())
            .forEach(k -> this.resources.put(k, Math.random() * 100));
    }

    public static Behavior<Messages.AuctionMessagesBidder> create() {
        return Behaviors.setup(app.BidAgent::new);
    }

    @Override
    public Receive<Messages.AuctionMessagesBidder> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.AnnounceAuction.class, this::onAnnounceAuction)
                .onMessage(Messages.AuctionState.class, this::onAuctionState)
                .onMessage(Messages.SendAuctionResult.class, this::onAuctionResult)
                .build();
    }

    private Behavior<Messages.AuctionMessagesBidder> onAnnounceAuction(Messages.AnnounceAuction msg){
        // LOGIC
        return this;
    }
    private Behavior<Messages.AuctionMessagesBidder> onAuctionState(Messages.AuctionState msg){
        // LOGIC
        return this;
    }
    private Behavior<Messages.AuctionMessagesBidder> onAuctionResult(Messages.SendAuctionResult msg){
        // LOGIC
        return this;
    }

}
