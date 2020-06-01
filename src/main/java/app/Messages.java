package app;

import akka.actor.typed.ActorRef;

import java.util.List;
import java.util.Map;


public class Messages {
    public interface RootMessages {}

    public static final class InitialiseEnvironment implements RootMessages {
        public final List<BiddingStrategy> strategies;

        public InitialiseEnvironment(List<BiddingStrategy> strategies) {
            this.strategies = strategies;
        }
    }

    // classified by who receives the message

    public interface AuctionMessagesRoot extends RootMessages{}

    public interface AuctionMessagesBidder {}

    public static final class AnnounceAuction implements AuctionMessagesBidder {
        public final AuctionType auctionType;
        public final Map<Resource, Double> resources;

        public AnnounceAuction(AuctionType auctionType, Map<Resource, Double> resources) {
            this.auctionType = auctionType;
            this.resources = resources;
        }
    }

    public static final class AuctionState implements AuctionMessagesBidder {
        public final Map<Resource, Double> highestBids;
        public final ActorRef<RootMessages> replyTo;

        public AuctionState(Map<Resource, Double> highestBids, ActorRef<RootMessages> replyTo) {
            this.highestBids = highestBids;
            this.replyTo = replyTo;
        }
    }

    public static final class SendAuctionResult implements AuctionMessagesBidder {
        public final AuctionResult result;
        public final Map<Resource, Double> toPay;

        public SendAuctionResult(AuctionResult result, Map<Resource, Double> toPay) {
            this.result = result;
            this.toPay = toPay;
        }
    }


    public static final class JoinAuction implements AuctionMessagesRoot {
        public final ActorRef<Messages.AuctionMessagesBidder> ref;

        public JoinAuction(ActorRef<Messages.AuctionMessagesBidder> ref) {
            this.ref =ref;
        }
    }

    public static final class PlaceBid implements AuctionMessagesRoot {
        public final Bid bid;

        public PlaceBid(Bid bid) {
            this.bid = bid;
        }
    }



}
