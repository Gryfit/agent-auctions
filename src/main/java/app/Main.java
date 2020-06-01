package app;

import akka.actor.typed.ActorSystem;

import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //#actor-system
        final ActorSystem<Messages.RootMessages> auctionSystem =
                ActorSystem.create(RootAgent.create(new VickreyAuction()), "auctionActorSystem");
        //#actor-system
        List<BiddingStrategy> strategies = new LinkedList<>();
        for (int i = 0; i < 1; i++) {
            strategies.add(new ExactlyPrivateValueStrategy());
            strategies.add(new LowerPrivateValueStrategy());
            strategies.add(new HigherPrivateValueStrategy());
        }
        //#main-send-messages
        auctionSystem.tell(new Messages.InitialiseEnvironment(strategies));
        //#main-send-messages

    }
}
