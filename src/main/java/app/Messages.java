package app;

import akka.actor.ActorRef;

import java.util.HashMap;


public class Messages {
    public interface RootMessages {}

    public static final class InitialiseEnvironment implements RootMessages {
        public final Integer size;

        public InitialiseEnvironment(Integer size) {
            this.size = size;
        }
    }

    // classified by who receives the message

    public interface AuctionMessagesRoot extends RootMessages{}

    public interface AuctionMessagesBidder {}

    public static final class AnnounceAuction implements AuctionMessagesBidder {
        public final AuctionType auctionType;
        public final HashMap<Resource, Double> resources;

        public AnnounceAuction(AuctionType auctionType, HashMap<Resource, Double> resources) {
            this.auctionType = auctionType;
            this.resources = resources;
        }
    }

    public static final class AuctionState implements AuctionMessagesBidder {
        public final HashMap<Resource, Double> highestBids;

        public AuctionState(HashMap<Resource, Double> highestBids) {
            this.highestBids = highestBids;
        }
    }

    public static final class SendAuctionResult implements AuctionMessagesBidder {
        public final AuctionResult result;

        public SendAuctionResult(AuctionResult result) {
            this.result = result;
        }
    }


    public static final class JoinAuction implements AuctionMessagesRoot {
        public final ActorRef ref;

        public JoinAuction(ActorRef ref) {
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
