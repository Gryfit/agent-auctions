package app;

import akka.actor.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.LinkedList;


public class RootAgent extends AbstractBehavior<Messages.RootMessages> {

    private BidHistory bidHistory = new BidHistory();
    private LinkedList<ActorRef> bidders = new LinkedList<>();

    public RootAgent(ActorContext<Messages.RootMessages> context) {
        super(context);
        context.spawn(RootAgent.create(), "root");
    }

    public static Behavior<Messages.RootMessages> create() {
        return Behaviors.setup(RootAgent::new);
    }


    @Override
    public Receive<Messages.RootMessages> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.InitialiseEnvironment.class, this::onInitialiseEnvironment)
                .onMessage(Messages.JoinAuction.class, this::onJoinAuction)
                .onMessage(Messages.PlaceBid.class, this::onPlaceBid)
                .build();
    }

    private Behavior<Messages.RootMessages> onInitialiseEnvironment(Messages.InitialiseEnvironment msg){
        for(int i=0; i<msg.size; i++){
            getContext().spawn(BidAgent.create(), "Bidder_"+i);
        }
        return this;
    }

    private Behavior<Messages.RootMessages> onJoinAuction(Messages.JoinAuction msg){
        bidders.add(msg.ref);
        // LOGIC
        return this;
    }

    private Behavior<Messages.RootMessages> onPlaceBid(Messages.PlaceBid msg){
        this.bidHistory.addBid(msg.bid);
        // LOGIC
        return this;
    }
}
